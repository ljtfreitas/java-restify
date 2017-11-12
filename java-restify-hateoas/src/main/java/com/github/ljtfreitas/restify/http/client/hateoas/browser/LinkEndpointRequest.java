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
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.util.Tryable;

public class LinkEndpointRequest {

	private final URL source;
	private final Link link;
	private final LinkURITemplateParameters parameters;
	private final Type responseType;
	private final String method;
	private final Headers headers;
	private final Object body;

	public LinkEndpointRequest(URL source, Link link) {
		this(source, link, LinkURITemplateParameters.empty());
	}

	public LinkEndpointRequest(Link link, Type responseType) {
		this(null, link, LinkURITemplateParameters.empty(), responseType);
	}

	public LinkEndpointRequest(URL source, Link link, LinkURITemplateParameters parameters) {
		this(source, link, parameters, void.class);
	}

	public LinkEndpointRequest(URL source, Link link, LinkURITemplateParameters parameters, Type responseType) {
		this(source, link, parameters, responseType, "GET");
	}

	public LinkEndpointRequest(URL source, Link link, LinkURITemplateParameters parameters, Type responseType,
			String method) {
		this(source, link, parameters, responseType, method, new Headers(), null);
	}

	public LinkEndpointRequest(URL source, Link link, LinkURITemplateParameters parameters, Type responseType,
			String method, Headers headers) {
		this(source, link, parameters, responseType, method, headers, null);
	}

	public LinkEndpointRequest(URL source, Link link, LinkURITemplateParameters parameters, Type responseType,
			String method, Headers headers, Object body) {
		this.source = source;
		this.link = link;
		this.parameters = parameters;
		this.responseType = responseType;
		this.method = method;
		this.headers = headers;
		this.body = body;
	}

	public EndpointRequest asEndpointRequest() {
		return new EndpointRequest(expand(), method, headers, body, responseType);
	}

	private URI expand() {
		URI href = new LinkURITemplate(link.href()).expand(parameters);

		URI endpoint = Optional.ofNullable(source)
			.map(s -> href.isAbsolute() ? href : Tryable.of(s::toURI).resolve(href))
				.orElse(href);
		return endpoint;
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("LinkEndpointRequest: [")
				.append("Link: ")
					.append(link.href())
				.append(", ")
				.append("Base URL: ")
					.append(source)
				.append(", ")
				.append("Response Type: ")
					.append(responseType)
				.append(", ")
					.append("Parameters: ")
					.append(parameters)
				.append(", ")
					.append("HTTP Method: ")
					.append(parameters)
				.append(", ")
					.append("Headers: ")
					.append(parameters)
			.append("]");

		return report.toString();
	}
}
