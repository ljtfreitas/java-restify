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

import java.util.Map;

import com.github.ljtfreitas.restify.http.client.message.ContentType;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;

public class Hop {

	private final String rel;
	private final LinkURITemplateParameters parameters;
	private final Headers headers;
	private final String method;
	private final Object body;

	private Hop(String rel) {
		this(rel, new LinkURITemplateParameters());
	}

	private Hop(String rel, LinkURITemplateParameters parameters) {
		this(rel, parameters, new Headers(), "GET", null);
	}

	private Hop(String rel, LinkURITemplateParameters parameters, Headers headers, String method, Object body) {
		this.rel = rel;
		this.parameters = parameters;
		this.headers = headers;
		this.method = method;
		this.body = body;
	}

	public String rel() {
		return rel;
	}

	public LinkURITemplateParameters parameters() {
		return parameters;
	}

	public Headers headers() {
		return headers;
	}

	public String method() {
		return method;
	}

	public Object body() {
		return body;
	}

	public Hop usingParameter(String name, String value) {
		LinkURITemplateParameters parameters = new LinkURITemplateParameters(this.parameters)
				.put(name, value);
		return new Hop(rel, parameters, headers, method, body);
	}

	public Hop usingParameter(LinkURITemplateParameter parameter) {
		LinkURITemplateParameters parameters = new LinkURITemplateParameters(this.parameters)
				.put(parameter);
		return new Hop(rel, parameters, headers, method, body);
	}

	public Hop usingParameters(LinkURITemplateParameters parameters) {
		return new Hop(rel, parameters, headers, method, body);
	}

	public Hop usingHeader(String name, String value) {
		Headers headers = this.headers.add(name, value);
		return new Hop(rel, parameters, headers, method, body);
	}

	public Hop usingHeader(Header header) {
		Headers headers = this.headers.add(header);
		return new Hop(rel, parameters, headers, method, body);
	}

	public Hop usingGet() {
		return new Hop(rel, parameters, headers, "GET", body);
	}

	public Hop usingPost() {
		return new Hop(rel, parameters, headers, "POST", body);
	}

	public Hop usingPost(Object body, ContentType contentType) {
		Headers headers = this.headers.add(new Header(Headers.CONTENT_TYPE, contentType.toString()));
		return new Hop(rel, parameters, headers, "POST", body);
	}

	public Hop usingPut() {
		return new Hop(rel, parameters, headers, "PUT", body);
	}

	public Hop usingPut(Object body, ContentType contentType) {
		Headers headers = this.headers.add(new Header(Headers.CONTENT_TYPE, contentType.toString()));
		return new Hop(rel, parameters, headers, "PUT", body);
	}

	public Hop usingDelete() {
		return new Hop(rel, parameters, headers, "DELETE", body);
	}

	public Hop usingMethod(String method) {
		return new Hop(rel, parameters, headers, method, body);
	}

	public Hop usingMethod(String method, Object body, ContentType contentType) {
		Headers headers = this.headers.add(new Header(Headers.CONTENT_TYPE, contentType.toString()));
		return new Hop(rel, parameters, headers, method, body);
	}

	public static Hop rel(String rel) {
		return new Hop(rel);
	}

	public static Hop rel(String rel, Map<String, String> parameters) {
		return new Hop(rel, new LinkURITemplateParameters(parameters));
	}

	public static Hop rel(String rel, LinkURITemplateParameters parameters) {
		return new Hop(rel, parameters);
	}

	public static Hop rel(String rel, LinkURITemplateParameter... parameters) {
		return new Hop(rel, new LinkURITemplateParameters(parameters));
	}
}
