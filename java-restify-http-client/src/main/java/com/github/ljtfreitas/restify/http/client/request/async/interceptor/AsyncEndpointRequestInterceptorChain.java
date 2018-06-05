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
package com.github.ljtfreitas.restify.http.client.request.async.interceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorChain;

public class AsyncEndpointRequestInterceptorChain {

	private final EndpointRequestInterceptorChain delegate;
	private final Collection<AsyncEndpointRequestInterceptor> interceptors;

	public AsyncEndpointRequestInterceptorChain(Collection<AsyncEndpointRequestInterceptor> interceptors) {
		this.delegate = new EndpointRequestInterceptorChain(interceptors);
		this.interceptors = new ArrayList<>(interceptors);
	}

	public EndpointRequest apply(EndpointRequest endpointRequest) {
		return delegate.apply(endpointRequest);
	}

	public CompletableFuture<EndpointRequest> applyAsync(EndpointRequest endpointRequest) {
		return interceptors.stream().reduce(CompletableFuture.completedFuture(endpointRequest), (r, i) -> i.interceptsAsync(r), (a, b) -> b);
	}

	public static AsyncEndpointRequestInterceptorChain of(Collection<EndpointRequestInterceptor> interceptors) {
		Collection<AsyncEndpointRequestInterceptor> all = interceptors
			.stream()
			.map(i -> (i instanceof AsyncEndpointRequestInterceptor) ? i : new AsyncEndpointRequestInterceptorAdapter(i))
			.map(AsyncEndpointRequestInterceptor.class::cast)
			.collect(Collectors.toList());

		return new AsyncEndpointRequestInterceptorChain(all);
	}

	private static class AsyncEndpointRequestInterceptorAdapter implements AsyncEndpointRequestInterceptor {

		private final EndpointRequestInterceptor delegate;

		private AsyncEndpointRequestInterceptorAdapter(EndpointRequestInterceptor delegate) {
			this.delegate = delegate;
		}

		@Override
		public EndpointRequest intercepts(EndpointRequest endpointRequest) {
			return delegate.intercepts(endpointRequest);
		}

		@Override
		public CompletableFuture<EndpointRequest> interceptsAsync(CompletableFuture<EndpointRequest> endpointRequest) {
			return endpointRequest.thenApplyAsync(r -> delegate.intercepts(r));
		}
	}
}
