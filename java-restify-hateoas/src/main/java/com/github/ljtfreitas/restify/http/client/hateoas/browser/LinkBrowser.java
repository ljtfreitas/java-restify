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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.hateoas.Resource;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;

public class LinkBrowser {

	private final LinkRequestExecutor linkRequestExecutor;
	private final URL baseURL;

	public LinkBrowser(LinkRequestExecutor linkRequestExecutor) {
		this(linkRequestExecutor, null);
	}

	public LinkBrowser(LinkRequestExecutor linkRequestExecutor, URL baseURL) {
		this.linkRequestExecutor = linkRequestExecutor;
		this.baseURL = baseURL;
	}

	public LinkBrowserTarget follow(Link link) {
		return new LinkBrowserTarget(link);
	}

	public class LinkBrowserTarget {

		private final Link link;
		private final Collection<Hop> relations = new ArrayList<>();
		private final LinkURITemplateParameters parameters = new LinkURITemplateParameters();

		private LinkBrowserTarget(Link link) {
			this.link = link;
		}

		public LinkBrowserTarget follow(String... rels) {
			Arrays.stream(rels).map(Hop::rel).forEach(relations::add);
			return this;
		}

		public LinkBrowserTarget follow(String rel, Map<String, String> parameters) {
			relations.add(Hop.rel(rel, parameters));
			return this;
		}

		public LinkBrowserTarget follow(String rel, LinkURITemplateParameters parameters) {
			relations.add(Hop.rel(rel, parameters));
			return this;
		}

		public LinkBrowserTarget follow(Hop... hops) {
			Arrays.stream(hops).forEach(relations::add);
			return this;
		}

		public <T> T as(Class<? extends T> type) {
			return execute(traverse(), type);
		}

		public <T> Collection<T> collectionOf(Class<? extends T> type) {
			return execute(traverse(), JavaType.parameterizedType(List.class, type));
		}

		private LinkURI traverse() {
			if (relations.isEmpty())
				return new LinkURI(link, parameters);
			return traverse(new LinkURI(link, parameters), relations.iterator());
		}

		private LinkURI traverse(LinkURI linkURI, Iterator<Hop> relations) {
			if (!relations.hasNext())
				return linkURI;

			Resource<String> resource = resourceOf(linkURI, String.class);

			Hop relation = relations.next();

			Link relationLink = resource.links().get(relation.rel())
				.orElseThrow(() -> new IllegalStateException("Expected to find link with rel [" + relation.rel() + "] "
						+ "in response [" + resource.content() + "]."));

			return traverse(new LinkURI(relationLink, relation.parameters()), relations);
		}

		private <T> Resource<T> resourceOf(LinkURI linkURI, Type type) {
			Type resourceType = JavaType.parameterizedType(Resource.class, type);

			return execute(linkURI, resourceType);
		}

		private <T> T execute(LinkURI linkURI, Type resourceType) {
			LinkEndpointRequest request = new LinkEndpointRequest(baseURL, linkURI.link, resourceType, linkURI.parameters);

			return linkRequestExecutor.execute(request);
		}

		private class LinkURI {

			private final Link link;
			private final LinkURITemplateParameters parameters;

			private LinkURI(Link link, LinkURITemplateParameters parameters) {
				this.link = link;
				this.parameters = parameters;
			}
		}
	}
}
