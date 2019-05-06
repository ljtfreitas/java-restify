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
package com.github.ljtfreitas.restify.http.client.request.grizzly;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

public class GrizzlyHttpClientRequestFactory implements AsyncHttpClientRequestFactory, Closeable {

	private final AsyncHttpClient asyncHttpClient;
	private final Charset charset;

	public GrizzlyHttpClientRequestFactory() {
		this(new AsyncHttpClient());
	}
	
	public GrizzlyHttpClientRequestFactory(AsyncHttpClient asyncHttpClient) {
		this(asyncHttpClient, Encoding.UTF_8.charset());
	}

	public GrizzlyHttpClientRequestFactory(Charset charset) {
		this(new AsyncHttpClient(), charset);
	}

	public GrizzlyHttpClientRequestFactory(AsyncHttpClientConfig asyncHttpClientConfig) {
		this(new AsyncHttpClient(asyncHttpClientConfig));
	}

	public GrizzlyHttpClientRequestFactory(AsyncHttpClientConfig asyncHttpClientConfig, Charset charset) {
		this(new AsyncHttpClient(asyncHttpClientConfig), charset);
	}

	public GrizzlyHttpClientRequestFactory(AsyncHttpClient asyncHttpClient, Charset charset) {
		this.asyncHttpClient = asyncHttpClient;
		this.charset = charset;
	}

	@Override
	public AsyncHttpClientRequest createAsyncOf(EndpointRequest endpointRequest) {
		return new GrizzlyHttpClientRequest(asyncHttpClient, endpointRequest, charset);
	}

	@Override
	public void close() throws IOException {
		asyncHttpClient.close();
	}
}
