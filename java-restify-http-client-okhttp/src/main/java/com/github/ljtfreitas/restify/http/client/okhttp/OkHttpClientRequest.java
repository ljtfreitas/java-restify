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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.util.Tryable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class OkHttpClientRequest implements AsyncHttpClientRequest {

	private final OkHttpClient okHttpClient;
	private final URI uri;
	private final String method;
	private final Headers headers;
	private final Charset charset;
	private final RequestBody body;

	public OkHttpClientRequest(OkHttpClient okHttpClient, URI uri, String method, Headers headers, Charset charset) {
		this(okHttpClient, uri, method, headers, charset, new RequestBody());
	}

	private OkHttpClientRequest(OkHttpClient okHttpClient, URI uri, String method, Headers headers, Charset charset,
			RequestBody body) {
		this.okHttpClient = okHttpClient;
		this.uri = uri;
		this.method = method;
		this.headers = headers;
		this.charset = charset;
		this.body = new RequestBody();
	}

	@Override
	public URI uri() {
		return uri;
	}

	@Override
	public String method() {
		return method;
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
		return new OkHttpClientRequest(okHttpClient, uri, method, headers.replace(header), charset);
	}

	@Override
	public HttpResponseMessage execute() throws HttpClientException {
		Request request = doBuildRequest();

		try {
			return responseOf(okHttpClient.newCall(request).execute());

		} catch (IOException e) {
			throw new HttpClientException("I/O error on HTTP request: [" + request.method() + " " +
					request.url() + "]", e);
		}
	}

	@Override
	public CompletableFuture<HttpResponseMessage> executeAsync() throws HttpClientException {
		CompletableFuture<HttpResponseMessage> future = new CompletableFuture<>();

		Request request = doBuildRequest();

		okHttpClient.newCall(request).enqueue(new Callback() {
			
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				future.complete(responseOf(response));
			}
			
			@Override
			public void onFailure(Call call, IOException exception) {
				future.completeExceptionally(new HttpClientException("I/O error on HTTP request: [" + request.method() + " " +
						request.url() + "]", exception));
			}
		});
		
		return future;
	}

	private Request doBuildRequest() {
		MediaType contentType = headers.get("Content-Type").map(header -> MediaType.parse(header.value()))
				.orElse(null);

		byte[] content = body.toByteArray();

		okhttp3.RequestBody body = (content.length > 0 ? okhttp3.RequestBody.create(contentType, content) : null);

		URL url = Tryable.of(() -> uri.toURL());

		Request.Builder builder = new Request.Builder();

		builder.url(url)
				.method(method, body);

		headers.forEach(h -> builder.addHeader(h.name(), h.value()));

		return builder.build();
	}

	private OkHttpClientResponse responseOf(Response response) {
		StatusCode statusCode = StatusCode.of(response.code(), response.message());

		Headers headers = response.headers().names().stream()
				.reduce(new Headers(), (a, b) -> a.add(b, response.headers(b)), (a, b) -> b);

		InputStream stream = response.body().byteStream();

		return new OkHttpClientResponse(statusCode, headers, stream, response, this);
	}

}
