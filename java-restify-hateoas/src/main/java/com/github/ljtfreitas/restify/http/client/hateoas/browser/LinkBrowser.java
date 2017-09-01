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

import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery.HypermediaLinkDiscovery;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery.RawResource;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.ContentType;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;

public class LinkBrowser {

	private final LinkRequestExecutor linkRequestExecutor;
	private final HypermediaLinkDiscovery resourceLinkDiscovery;
	private final URL baseURL;

	public LinkBrowser(LinkRequestExecutor linkRequestExecutor) {
		this(linkRequestExecutor, HypermediaLinkDiscovery.all(), null);
	}

	public LinkBrowser(LinkRequestExecutor linkRequestExecutor, HypermediaLinkDiscovery resourceLinkDiscovery) {
		this(linkRequestExecutor, resourceLinkDiscovery, null);
	}

	public LinkBrowser(LinkRequestExecutor linkRequestExecutor, HypermediaLinkDiscovery resourceLinkDiscovery, URL baseURL) {
		this.linkRequestExecutor = linkRequestExecutor;
		this.resourceLinkDiscovery = resourceLinkDiscovery;
		this.baseURL = baseURL;
	}

	public LinkBrowserTraverson follow(Link link) {
		return new LinkBrowserTraverson(link);
	}

	public LinkBrowserTraverson follow(Link link, LinkURITemplateParameters parameters) {
		return new LinkBrowserTraverson(link, parameters);
	}

	public LinkBrowserTraverson follow(Link link, LinkURITemplateParameter... parameters) {
		return new LinkBrowserTraverson(link, new LinkURITemplateParameters(parameters));
	}

	public class LinkBrowserTraverson {

		private final Link link;
		private final LinkURITemplateParameters parameters;

		private final Queue<Hop> relations = new LinkedList<>();

		private LinkBrowserTraverson(Link link) {
			this.link = link;
			this.parameters = new LinkURITemplateParameters();
		}

		private LinkBrowserTraverson(Link link, LinkURITemplateParameters parameters) {
			this.link = link;
			this.parameters = parameters;
		}

		public LinkBrowserTraverson follow(String... rels) {
			Arrays.stream(rels).map(Hop::rel).forEach(relations::add);
			return this;
		}

		public LinkBrowserTraverson follow(String rel, Map<String, String> parameters) {
			relations.add(Hop.rel(rel, parameters));
			return this;
		}

		public LinkBrowserTraverson follow(String rel, LinkURITemplateParameters parameters) {
			relations.add(Hop.rel(rel, parameters));
			return this;
		}

		public LinkBrowserTraverson follow(Hop... hops) {
			Arrays.stream(hops).forEach(relations::add);
			return this;
		}

		public <T> T as(Class<? extends T> type) {
			return tryExecute(type);
		}

		public <T> Collection<T> asCollectionOf(Class<? extends T> type) {
			return tryExecute(JavaType.parameterizedType(List.class, type));
		}

		private <T> T tryExecute(Type responseType) {
			try {
				EndpointResponse<T> response = execute(traverse(), responseType);
				return response.body();
			} catch (Exception e) {
				throw new LinkBrowserException("Could not follow link [" + link + "]", e);
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
