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

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.protocol.HttpContext;

import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;

public class ApacheAsyncHttpClientRequestFactory implements AsyncHttpClientRequestFactory, Closeable {

	private final HttpAsyncClient httpAsyncClient;
	private final RequestConfig requestConfig;
	private final HttpContext httpContext;
	private final Charset charset;
	private final ApacheHttpClientRequestFactory delegate;

	public ApacheAsyncHttpClientRequestFactory() {
		this(HttpAsyncClients.createSystem(), null);
	}

	public ApacheAsyncHttpClientRequestFactory(HttpAsyncClient httpAsyncClient) {
		this(httpAsyncClient, null);
	}

	public ApacheAsyncHttpClientRequestFactory(RequestConfig requestConfig) {
		this(HttpAsyncClients.createSystem(), requestConfig);
	}

	public ApacheAsyncHttpClientRequestFactory(HttpContext httpContext) {
		this(HttpAsyncClients.createSystem(), null, httpContext);
	}

	public ApacheAsyncHttpClientRequestFactory(Charset charset) {
		this(HttpAsyncClients.createSystem(), null, null, charset);
	}

	public ApacheAsyncHttpClientRequestFactory(HttpAsyncClient httpAsyncClient, RequestConfig requestConfig) {
		this(httpAsyncClient, requestConfig, null, HttpClients.createSystem());
	}

	public ApacheAsyncHttpClientRequestFactory(HttpAsyncClient httpAsyncClient, RequestConfig requestConfig, HttpClient httpClient) {
		this(httpAsyncClient, requestConfig, null, httpClient);
	}

	public ApacheAsyncHttpClientRequestFactory(HttpAsyncClient httpAsyncClient, RequestConfig requestConfig, HttpContext httpContext) {
		this(httpAsyncClient, requestConfig, httpContext, Encoding.UTF_8.charset(), HttpClients.createSystem());
	}

	public ApacheAsyncHttpClientRequestFactory(HttpAsyncClient httpAsyncClient, RequestConfig requestConfig, HttpContext httpContext,
			HttpClient httpClient) {
		this(httpAsyncClient, requestConfig, httpContext, Encoding.UTF_8.charset(), httpClient);
	}

	public ApacheAsyncHttpClientRequestFactory(HttpAsyncClient httpClient, RequestConfig requestConfig, HttpContext httpContext, Charset charset) {
		this(httpClient, requestConfig, httpContext, charset, HttpClients.createSystem());
	}

	public ApacheAsyncHttpClientRequestFactory(HttpAsyncClient httpAsyncClient, RequestConfig requestConfig, HttpContext httpContext,
			Charset charset, HttpClient httpClient) {
		this.httpAsyncClient = httpAsyncClient;
		this.requestConfig = requestConfig;
		this.httpContext = httpContext;
		this.charset = charset;
		this.delegate = new ApacheHttpClientRequestFactory(httpClient, requestConfig, httpContext, charset);
	}

	@Override
	public ApacheHttpClientRequest createOf(EndpointRequest endpointRequest) {
		return delegate.createOf(endpointRequest);
	}

	@Override
	public ApacheAsyncHttpClientRequest createAsyncOf(EndpointRequest endpointRequest) {
		HttpUriRequest httpRequest = HttpUriRequestStrategy.of(endpointRequest.method())
				.create(endpointRequest.endpoint().toString());

		HttpContext context = new HttpContextBuilder(httpContext, requestConfig, httpAsyncClient).buildTo(endpointRequest);

		return new ApacheAsyncHttpClientRequest(httpAsyncClient, httpRequest, context, charset, endpointRequest.headers());
	}

	@Override
	public void close() throws IOException {
		if (httpAsyncClient instanceof Closeable) {
			((Closeable) this.httpAsyncClient).close();
		}
	}
}
