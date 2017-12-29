/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.hateoas.browser;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.hateoas.Resource;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery.HypermediaLinkDiscovery;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery.RawResource;
import com.github.ljtfreitas.restify.http.client.message.ContentType;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class HypermediaBrowser {

	private final LinkRequestExecutor linkRequestExecutor;
	private final HypermediaLinkDiscovery resourceLinkDiscovery;
	private final URL baseURL;

	public HypermediaBrowser(LinkRequestExecutor linkRequestExecutor) {
		this(linkRequestExecutor, HypermediaLinkDiscovery.all(), null);
	}

	public HypermediaBrowser(LinkRequestExecutor linkRequestExecutor, HypermediaLinkDiscovery resourceLinkDiscovery) {
		this(linkRequestExecutor, resourceLinkDiscovery, null);
	}

	public HypermediaBrowser(LinkRequestExecutor linkRequestExecutor, HypermediaLinkDiscovery resourceLinkDiscovery, URL baseURL) {
		this.linkRequestExecutor = linkRequestExecutor;
		this.resourceLinkDiscovery = resourceLinkDiscovery;
		this.baseURL = baseURL;
	}

	public HypermediaBrowserTraverson follow(Link link) {
		return new HypermediaBrowserTraverson(link);
	}

	public HypermediaBrowserTraverson follow(Link link, LinkURITemplateParameters parameters) {
		return new HypermediaBrowserTraverson(link, parameters);
	}

	public HypermediaBrowserTraverson follow(Link link, LinkURITemplateParameter... parameters) {
		return new HypermediaBrowserTraverson(link, new LinkURITemplateParameters(parameters));
	}

	public class HypermediaBrowserTraverson {

		private final Link link;
		private final LinkURITemplateParameters parameters;

		private final Queue<Hop> relations = new LinkedList<>();

		private HypermediaBrowserTraverson(Link link) {
			this.link = link;
			this.parameters = new LinkURITemplateParameters();
		}

		private HypermediaBrowserTraverson(Link link, LinkURITemplateParameters parameters) {
			this.link = link;
			this.parameters = parameters;
		}

		public HypermediaBrowserTraverson follow(String... rels) {
			Arrays.stream(rels).map(Hop::rel).forEach(relations::add);
			return this;
		}

		public HypermediaBrowserTraverson follow(String rel, Map<String, String> parameters) {
			relations.add(Hop.rel(rel, parameters));
			return this;
		}

		public HypermediaBrowserTraverson follow(String rel, LinkURITemplateParameters parameters) {
			relations.add(Hop.rel(rel, parameters));
			return this;
		}

		public HypermediaBrowserTraverson follow(Hop... hops) {
			Arrays.stream(hops).forEach(relations::add);
			return this;
		}

		public <T> T as(Type type) {
			EndpointResponse<T> response = tryExecute(type);
			return response.body();
		}

		public <T> Collection<T> asCollectionOf(Type type) {
			EndpointResponse<List<T>> response = tryExecute(JavaType.parameterizedType(List.class, type));
			return response.body();
		}

		public <T> Resource<T> asResourceOf(Type type) {
			EndpointResponse<Resource<T>> response = tryExecute(JavaType.parameterizedType(Resource.class, type));
			return response.body();
		}

		public <T> EndpointResponse<T> responseAs(Type type) {
			return tryExecute(type);
		}

		public EndpointResponse<Void> execute() {
			return tryExecute(void.class);
		}
		
		private <T> EndpointResponse<T> tryExecute(Type responseType) {
			try {
				return execute(traverse(), responseType);
			} catch (Exception e) {
				throw new HypermediaBrowserException("Could not follow link [" + link + "]", e);
			}
		}

		private LinkURI traverse() {
			LinkURI linkURI = new LinkURI(link, parameters);
			if (relations.isEmpty()) return linkURI;
			return traverse(linkURI, relations.poll());
		}

		private LinkURI traverse(LinkURI linkURI, Hop relation) {
			if (relation == null) return linkURI;

			EndpointResponse<String> resource = execute(linkURI, String.class);

			String resourceBody = resource.body();

			ContentType contentType = resource.headers().get(Headers.CONTENT_TYPE)
					.map(h -> ContentType.of(h.value()))
						.orElseThrow(() -> new IllegalArgumentException("Your response body does not have a Content-Type header?"));

			Link relationLink = resourceLinkDiscovery.discovery(relation.rel(), RawResource.of(resourceBody, contentType))
				.orElseThrow(() -> new IllegalStateException("Expected to find link [" + relation.rel() + "] "
						+ "in resource [" + resourceBody + "]."));

			return traverse(new LinkURI(relationLink, relation), relations.poll());
		}

		private <T> EndpointResponse<T> execute(LinkURI linkURI, Type responseType) {
			LinkEndpointRequest request = new LinkEndpointRequest(baseURL, linkURI.link, linkURI.parameters, responseType, linkURI.method,
					linkURI.headers, linkURI.body);

			return linkRequestExecutor.execute(request);
		}

		private class LinkURI {

			private final Link link;
			private final LinkURITemplateParameters parameters;
			private final Headers headers;
			private final String method;
			private final Object body;

			private LinkURI(Link link, Hop relation) {
				this.link = link;
				this.parameters = relation.parameters();
				this.headers = relation.headers();
				this.method = relation.method();
				this.body = relation.body();
			}

			private LinkURI(Link link, LinkURITemplateParameters parameters) {
				this.link = link;
				this.parameters = parameters;
				this.headers = new Headers();
				this.method = "GET";
				this.body = null;
			}
		}
	}
}
