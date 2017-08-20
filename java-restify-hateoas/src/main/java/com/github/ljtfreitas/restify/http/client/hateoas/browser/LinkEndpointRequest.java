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
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;
import com.github.ljtfreitas.restify.http.util.Tryable;

public class LinkEndpointRequest {

	private final URL source;
	private final Link link;
	private final JavaType responseType;
	private final LinkURITemplateParameters parameters;

	public LinkEndpointRequest(URL source, Link link, Type responseType) {
		this(source, link, responseType, new LinkURITemplateParameters());
	}

	public LinkEndpointRequest(URL source, Link link, JavaType responseType) {
		this(source, link, responseType, new LinkURITemplateParameters());
	}

	public LinkEndpointRequest(Link link, Type responseType) {
		this(link, responseType, new LinkURITemplateParameters());
	}

	public LinkEndpointRequest(Link link, JavaType responseType) {
		this(link, responseType, new LinkURITemplateParameters());
	}

	public LinkEndpointRequest(Link link, Type responseType, LinkURITemplateParameters parameters) {
		this(null, link, responseType, parameters);
	}

	public LinkEndpointRequest(Link link, JavaType responseType, LinkURITemplateParameters parameters) {
		this(null, link, responseType, parameters);
	}

	public LinkEndpointRequest(URL source, Link link, Type responseType, LinkURITemplateParameters parameters) {
		this(source, link, JavaType.of(responseType), parameters);
	}

	public LinkEndpointRequest(URL source, Link link, JavaType responseType, LinkURITemplateParameters parameters) {
		this.source = source;
		this.link = link;
		this.responseType = responseType;
		this.parameters = parameters;
	}

	public URI expand() {
		URI href = new LinkURITemplate(link.href()).expand(parameters);

		URI endpoint = Optional.ofNullable(source)
			.map(s -> Tryable.of(s::toURI).relativize(href))
				.orElse(href);

		return endpoint;
	}

	public JavaType responseType() {
		return responseType;
	}
}
