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
package com.github.ljtfreitas.restify.http.client.request;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.util.Tryable;

public class EndpointRequest {

	private final URI endpoint;
	private final String method;
	private final Headers headers;
	private final Object body;
	private final JavaType responseType;
	private final EndpointVersion version;
	private final EndpointRequestMetadata metadata;

	public EndpointRequest(URI endpoint, String method) {
		this(endpoint, method, (EndpointVersion) null);
	}

	public EndpointRequest(URI endpoint, String method, EndpointVersion version) {
		this(endpoint, method, void.class, version);
	}

	public EndpointRequest(URI endpoint, String method, Type responseType) {
		this(endpoint, method, responseType, (EndpointVersion) null);
	}

	public EndpointRequest(URI endpoint, String method, JavaType responseType) {
		this(endpoint, method, responseType, null);
	}

	public EndpointRequest(URI endpoint, String method, Type responseType, EndpointVersion version) {
		this(endpoint, method, new Headers(), responseType, version);
	}

	public EndpointRequest(URI endpoint, String method, JavaType responseType, EndpointVersion version) {
		this(endpoint, method, new Headers(), null, responseType, version);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Type responseType) {
		this(endpoint, method, headers, responseType, (EndpointVersion) null);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, JavaType responseType) {
		this(endpoint, method, headers, responseType, (EndpointVersion) null);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Type responseType, EndpointVersion version) {
		this(endpoint, method, headers, null, responseType, version);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, JavaType responseType,
			EndpointVersion version) {
		this(endpoint, method, headers, null, responseType, version);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Object body) {
		this(endpoint, method, headers, body, (EndpointVersion) null);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Object body, EndpointVersion version) {
		this(endpoint, method, headers, body, void.class, version);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Object body, Type responseType) {
		this(endpoint, method, headers, body, responseType, null);
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Object body, Type responseType,
			EndpointVersion version) {
		this(endpoint, method, headers, body, JavaType.of(responseType), version, EndpointRequestMetadata.empty());
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Object body, JavaType responseType) {
		this(endpoint, method, headers, body, responseType, null, EndpointRequestMetadata.empty());
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Object body, JavaType responseType,
			EndpointVersion version) {
		this(endpoint, method, headers, body, responseType, null, EndpointRequestMetadata.empty());
	}

	public EndpointRequest(URI endpoint, String method, Headers headers, Object body, Type responseType,
			EndpointVersion version, EndpointRequestMetadata metadata) {
		this(endpoint, method, headers, body, JavaType.of(responseType), version, metadata);
	}
	
	public EndpointRequest(URI endpoint, String method, Headers headers, Object body, JavaType responseType,
			EndpointVersion version, EndpointRequestMetadata metadata) {
		this.endpoint = endpoint;
		this.method = method;
		this.headers = headers;
		this.body = body;
		this.responseType = responseType;
		this.version = version;
		this.metadata = metadata;
	}

	public URI endpoint() {
		return endpoint;
	}

	public String method() {
		return method;
	}

	public Optional<Object> body() {
		return Optional.ofNullable(body);
	}

	public Headers headers() {
		return headers;
	}

	public JavaType responseType() {
		return responseType;
	}

	public Optional<EndpointVersion> version() {
		return Optional.ofNullable(version);
	}

	public EndpointRequestMetadata metadata() {
		return metadata;
	}

	public EndpointRequest append(Parameters parameters) {
		String query = parameters.queryString();
		return appendOnQuery(query);
	}

	public EndpointRequest add(Header header) {
		Headers newheaders = headers.add(header);

		return new EndpointRequest(endpoint, method, newheaders, body, responseType, version, metadata);
	}

	public EndpointRequest add(Headers headers) {
		return new EndpointRequest(endpoint, method, this.headers.addAll(headers), body, responseType, version,
				metadata);
	}

	public EndpointRequest replace(Header header) {
		Headers newheaders = headers.replace(header);

		return new EndpointRequest(endpoint, method, newheaders, body, responseType, version, metadata);
	}
	
	private EndpointRequest appendOnQuery(String query) {
		String appender = endpoint.getQuery() == null ? "" : "&";

		String newQuery = Optional.ofNullable(endpoint.getRawQuery())
				.orElse("")
					.concat(appender)
						.concat(query);

		return Tryable.of(() -> cloneWithQuery(newQuery), (e) -> new IllegalArgumentException(e));
	}

	private EndpointRequest cloneWithQuery(String query) throws URISyntaxException {
		URI newURI = new URI(endpoint.getScheme(), endpoint.getRawAuthority(), endpoint.getRawPath(),
				query, endpoint.getRawFragment());

		return new EndpointRequest(newURI, method, headers, body, responseType, version, metadata);
	}

	public EndpointRequest replace(URI endpoint) {
		return new EndpointRequest(endpoint, method, headers, body, responseType, version, metadata);
	}

	public EndpointRequest usingBody(Object body) {
		return new EndpointRequest(endpoint, method, headers, body, responseType, version, metadata);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("EndpointRequest: [")
				.append("URI: ")
					.append(endpoint)
				.append(", ")
				.append("HTTP Method: ")
					.append(method)
				.append(", ")
				.append("Body: ")
					.append(body)
				.append(", ")
					.append("Response Type: ")
					.append(responseType)
			.append("]");

		return report.toString();
	}
}
