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
package com.github.ljtfreitas.restify.http.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;

class StreamingJdkHttpClientRequest extends BaseJdkHttpClientRequest {

	private final HttpURLConnection connection;

	StreamingJdkHttpClientRequest(HttpURLConnection connection, Charset charset, Headers headers, HttpRequestBody body,
			HttpClientRequestConfiguration configuration) {
		super(connection, charset, headers, body, configuration);
		this.connection = connection;
	}

	@Override
	public HttpClientResponse execute() throws HttpClientException {
		try {
			body().output().close();

			connection.connect();

			return responseOf(connection);

		} catch (IOException e) {
			throw new HttpClientException("I/O error on HTTP request: [" + connection.getRequestMethod() + " " +
					connection.getURL() + "]", e);
		}
	}

	@Override
	HttpRequestMessage doReplace(HttpURLConnection connection, Charset charset, Headers newHeaders,
			HttpRequestBody body, HttpClientRequestConfiguration configuration) {
		StreamingJdkHttpRequestBody newBody = StreamingJdkHttpRequestBody.create(connection, newHeaders, configuration);
		return new StreamingJdkHttpClientRequest(connection, charset, newHeaders, newBody, configuration);
	}

}
