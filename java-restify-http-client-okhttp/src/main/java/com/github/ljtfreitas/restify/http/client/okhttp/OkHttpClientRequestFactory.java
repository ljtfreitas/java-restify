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
package com.github.ljtfreitas.restify.http.client.okhttp;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;

import okhttp3.OkHttpClient;

public class OkHttpClientRequestFactory implements AsyncHttpClientRequestFactory, Closeable {

	private final OkHttpClient okHttpClient;
	private final Charset charset;

	public OkHttpClientRequestFactory() {
		this(new OkHttpClient());
	}

	public OkHttpClientRequestFactory(OkHttpClient okHttpClient) {
		this(okHttpClient, Encoding.UTF_8.charset());
	}

	public OkHttpClientRequestFactory(Charset charset) {
		this(new OkHttpClient(), charset);
	}

	public OkHttpClientRequestFactory(OkHttpClient okHttpClient, Charset charset) {
		this.okHttpClient = okHttpClient;
		this.charset = charset;
	}

	@Override
	public OkHttpClientRequest createOf(EndpointRequest endpointRequest) {
		return new OkHttpClientRequest(okHttpClient, endpointRequest.endpoint(), endpointRequest.method(), endpointRequest.headers(),
				charset);
	}

	@Override
	public AsyncHttpClientRequest createAsyncOf(EndpointRequest endpointRequest) {
		return new OkHttpClientRequest(okHttpClient, endpointRequest.endpoint(), endpointRequest.method(), endpointRequest.headers(),
				charset);
	}
	
	@Override
	public void close() throws IOException {
		if (okHttpClient.cache() != null) {
			okHttpClient.cache().close();
		}

		okHttpClient.dispatcher().executorService().shutdown();
	}
}
