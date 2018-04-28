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
package com.github.ljtfreitas.restify.http.spring.client.request.async;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestOperations;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.spring.client.request.EndpointResponseConverter;
import com.github.ljtfreitas.restify.http.spring.client.request.ErrorHttpResponseMessage;
import com.github.ljtfreitas.restify.http.spring.client.request.RequestEntityConverter;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class AsyncRestOperationsEndpointRequestExecutor implements AsyncEndpointRequestExecutor {

	private final AsyncRestOperations rest;
	private final EndpointResponseErrorFallback endpointResponseErrorFallback;
	private final RequestEntityConverter requestEntityConverter;
	private final EndpointResponseConverter responseEntityConverter;

	public AsyncRestOperationsEndpointRequestExecutor(AsyncRestOperations rest) {
		this(rest, new DefaultEndpointResponseErrorFallback());
	}

	public AsyncRestOperationsEndpointRequestExecutor(AsyncRestOperations rest, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this(rest, new RequestEntityConverter(), new EndpointResponseConverter(), endpointResponseErrorFallback);
	}

	public AsyncRestOperationsEndpointRequestExecutor(AsyncRestOperations rest, RequestEntityConverter requestEntityConverter,
			EndpointResponseConverter responseEntityConverter, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.rest = rest;
		this.requestEntityConverter = requestEntityConverter;
		this.responseEntityConverter = responseEntityConverter;
		this.endpointResponseErrorFallback = endpointResponseErrorFallback;
	}

	@Override
	public <T> CompletableFuture<EndpointResponse<T>> executeAsync(EndpointRequest source) {
		RequestEntity<Object> request = requestEntityConverter.convert(source);
		return doExecuteAsync(source, request);
	}

	private <T> CompletableFuture<EndpointResponse<T>> doExecuteAsync(EndpointRequest source, RequestEntity<Object> request) {
		ListenableFuture<ResponseEntity<Object>> response = rest.exchange(request.getUrl(), request.getMethod(), request,
				new JavaTypeReference(source.responseType()));

		CompletableFuture<EndpointResponse<T>> responseAsFuture = new CompletableFuture<>();

		response.addCallback(new ListenableFutureCallback<ResponseEntity<Object>>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(ResponseEntity<Object> result) {
				EndpointResponse<T> converted = (EndpointResponse<T>) responseEntityConverter.convert(result);
				responseAsFuture.complete(converted);
			}

			@Override
			public void onFailure(Throwable ex) {
				try {
					responseAsFuture.complete(handleException(source, request, ex));
				} catch (Exception e) {
					responseAsFuture.completeExceptionally(e);
				}
			}
		});

		return responseAsFuture;
	}

	private <T> EndpointResponse<T> handleException(EndpointRequest source, RequestEntity<Object> request, Throwable throwable) {
		Exception cause = (Exception) (throwable instanceof CompletionException || throwable instanceof ExecutionException ?
				throwable.getCause() : throwable);

		if (cause instanceof HttpException) {
			throw (HttpException) cause;

		} else if (cause instanceof RestClientResponseException) {
			return endpointResponseErrorFallback.onError(ErrorHttpResponseMessage.from(request, (RestClientResponseException) cause), source.responseType());

		} else if (cause instanceof ResourceAccessException) {
			throw new HttpClientException("I/O error on HTTP request: [" + request.getMethod() + " " + request.getUrl() + "]", cause);

		} else {
			throw new HttpException("Error on HTTP request: [" + request.getMethod() + " " + request.getUrl() + "]", cause);
		}
	}

	@Override
	public <T> EndpointResponse<T> execute(EndpointRequest source) {
		RequestEntity<Object> request = requestEntityConverter.convert(source);

		try {
			CompletableFuture<EndpointResponse<T>> responseAsFuture = doExecuteAsync(source, request);

			return responseAsFuture.join();

		} catch (CompletionException e) {
			return handleException(source, request, e);
		}
	}

	private class JavaTypeReference extends ParameterizedTypeReference<Object> {

		private final JavaType type;

		public JavaTypeReference(JavaType type) {
			this.type = type;
		}

		@Override
		public Type getType() {
			return type.unwrap();
		}

		@Override
		public boolean equals(Object obj) {
			if ((obj instanceof JavaTypeReference)) return false;
			JavaTypeReference that = (JavaTypeReference) obj;
			return this == that || this.type.equals(that.type);
		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

		@Override
		public String toString() {
			return "JavaTypeReference<" + type + ">";
		}
	}
}
