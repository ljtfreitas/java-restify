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
package com.github.ljtfreitas.restify.http.client.hateoas;

import java.util.Collection;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Resource<T> {

	@JsonUnwrapped
	private T content;

	@JsonProperty("links")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonDeserialize(using = HypermediaLinksDeserializer.class)
	private Links links = new Links();

	@JsonProperty(value = "resource", access = Access.WRITE_ONLY)
	private Embedded embedded;

	@Deprecated
	Resource() {
	}

	public Resource(T content) {
		this.content = content;
	}

	public Resource(T content, Links links) {
		this(content, links, null);
	}

	public Resource(T content, Collection<Link> links) {
		this(content, new Links(links), null);
	}

	private Resource(T content, Links links, Embedded embedded) {
		this.content = content;
		this.links = links;
		this.embedded = embedded;
	}

	public T content() {
		return content;
	}

	public Optional<Embedded> embedded() {
		return Optional.ofNullable(embedded);
	}

	public Links links() {
		return links;
	}

	public Resource<T> addLink(Link link) {
		Links links = this.links.add(link);
		return new Resource<>(content, links, embedded);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("Resource: [")
				.append("Content: ")
					.append(content)
				.append(", ")
				.append("Links: ")
					.append(links)
			.append("]");

		return report.toString();
	}
}
