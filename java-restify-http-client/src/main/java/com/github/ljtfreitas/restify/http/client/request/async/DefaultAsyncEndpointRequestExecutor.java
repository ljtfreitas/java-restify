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
package com.github.ljtfreitas.restify.http.client.request.async;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageException;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class DefaultAsyncEndpointRequestExecutor implements AsyncEndpointRequestExecutor {

	private final Executor executor;
	private final AsyncHttpClientRequestFactory httpClientRequestFactory;
	private final EndpointRequestWriter endpointRequestWriter;
	private final EndpointResponseReader endpointResponseReader;
	private final EndpointRequestExecutor delegate;

	public DefaultAsyncEndpointRequestExecutor(Executor executor, AsyncHttpClientRequestFactory httpClientRequestFactory,
			EndpointRequestWriter endpointRequestWriter, EndpointResponseReader endpointResponseReader,
			EndpointRequestExecutor delegate) {
		this.executor = executor;
		this.httpClientRequestFactory = httpClientRequestFactory;
		this.endpointRequestWriter = endpointRequestWriter;
		this.endpointResponseReader = endpointResponseReader;
		this.delegate = delegate;
	}

	@Override
	public <T> EndpointResponse<T> execute(EndpointRequest endpointRequest) {
		return delegate.execute(endpointRequest);
	}

	@Override
	public <T> CompletableFuture<EndpointResponse<T>> executeAsync(EndpointRequest endpointRequest) {
		CompletableFuture<EndpointResponse<T>> future = doExecute(endpointRequest)
				.thenApplyAsync(response -> doRead(response, endpointRequest.responseType()), executor);

		return future.handleAsync((r, e) -> doHandle(r, deepCause(e), endpointRequest), executor);
	}

	private CompletableFuture<HttpResponseMessage> doExecute(EndpointRequest endpointRequest) {
		AsyncHttpClientRequest httpClientRequest = httpClientRequestFactory.createAsyncOf(endpointRequest);

		endpointRequest.body().ifPresent(b -> endpointRequestWriter.write(endpointRequest, httpClientRequest));

		return httpClientRequest.executeAsync();
	}

	private <T> EndpointResponse<T> doRead(HttpResponseMessage response, JavaType responseType) {
		return endpointResponseReader.read(response, responseType);
	}

	private Throwable deepCause(Throwable throwable) {
		return (throwable instanceof CompletionException) ? deepCause(throwable.getCause()) : throwable;
	}

	private <T> EndpointResponse<T> doHandle(EndpointResponse<T> response, Throwable exception, EndpointRequest endpointRequest) {
		if (response != null) {
			return response;

		} else if (exception instanceof HttpClientException) {
			HttpClientException httpClientException = (HttpClientException) exception;
			throw httpClientException;

		} else if (exception instanceof HttpMessageException) {
			HttpMessageException httpMessageException = (HttpMessageException) exception;
			throw httpMessageException;

		} else if (exception instanceof IOException) {
			throw new HttpClientException("I/O error on HTTP request: [" + endpointRequest.method() + " " +
					endpointRequest.endpoint() + "]", exception);

		} else {
			throw new HttpException("Error on HTTP request: [" + endpointRequest.method() + " " +
					endpointRequest.endpoint() + "]", exception);
		}
	}
}
