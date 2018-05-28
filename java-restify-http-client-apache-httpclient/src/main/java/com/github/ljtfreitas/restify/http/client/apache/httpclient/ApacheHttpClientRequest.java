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
package com.github.ljtfreitas.restify.http.client.apache.httpclient;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;

class ApacheHttpClientRequest implements HttpClientRequest {

	private final HttpClient httpClient;
	private final HttpUriRequest httpRequest;
	private final HttpContext httpContext;
	private final Charset charset;
	private final Headers headers;

	private final RequestBody body;

	public ApacheHttpClientRequest(HttpClient httpClient, HttpUriRequest httpRequest, HttpContext httpContext, Charset charset,
			Headers headers) {
		this(httpClient, httpRequest, httpContext, charset, headers, new RequestBody());
	}

	private ApacheHttpClientRequest(HttpClient httpClient, HttpUriRequest httpRequest, HttpContext httpContext, Charset charset,
			Headers headers, RequestBody body) {
		this.httpClient = httpClient;
		this.httpRequest = httpRequest;
		this.httpContext = httpContext;
		this.charset = charset;
		this.headers = headers;
		this.body = body;
	}

	@Override
	public HttpResponseMessage execute() throws HttpClientException {
		headers.all().forEach(h -> httpRequest.addHeader(h.name(), h.value()));

		if (httpRequest instanceof HttpEntityEnclosingRequest) {
			HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
			HttpEntity requestEntity = new ByteArrayEntity(body.toByteArray());
			entityEnclosingRequest.setEntity(requestEntity);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpRequest, httpContext);

			return responseOf(httpResponse);

		} catch (IOException e) {
			throw new HttpClientException("I/O error on HTTP request: [" + httpRequest.getMethod() + " " +
					httpRequest.getURI() + "]", e);
		}
	}

	private ApacheHttpClientResponse responseOf(HttpResponse httpResponse) throws IOException {
		ApacheHttpResponseReader reader = new ApacheHttpResponseReader(httpResponse, this);

		return reader.read();
	}

	@Override
	public URI uri() {
		return httpRequest.getURI();
	}

	@Override
	public String method() {
		return httpRequest.getMethod();
	}

	@Override
	public RequestBody body() {
		return body;
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public Headers headers() {
		return headers;
	}

	@Override
	public HttpRequestMessage replace(Header header) {
		return new ApacheHttpClientRequest(httpClient, httpRequest, httpContext, charset, headers.replace(header),
				body);
	}
}
