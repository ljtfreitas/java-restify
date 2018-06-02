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
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestAdapter;
import com.github.ljtfreitas.restify.http.client.request.interceptor.HttpClientRequestInterceptor;

public class AsyncHttpClientRequestInterceptorChain {

	private final Collection<AsyncHttpClientRequestInterceptor> interceptors;

	public AsyncHttpClientRequestInterceptorChain(Collection<AsyncHttpClientRequestInterceptor> interceptors) {
		this.interceptors = new ArrayList<>(interceptors);
	}

	public AsyncHttpClientRequest apply(AsyncHttpClientRequest asyncHttpClientRequest) {
		return interceptors.stream().reduce(asyncHttpClientRequest, (r, i) -> i.interceptsAsync(r), (a, b) -> b);
	}

	public static AsyncHttpClientRequestInterceptorChain of(Collection<? extends HttpClientRequestInterceptor> interceptors) {
		Collection<AsyncHttpClientRequestInterceptor> all = interceptors
			.stream()
			.map(i -> (i instanceof AsyncHttpClientRequestInterceptor) ? i : new AsyncHttpClientRequestInterceptorAdapter(i))
			.map(AsyncHttpClientRequestInterceptorAdapter.class::cast)
			.collect(Collectors.toList());

		return new AsyncHttpClientRequestInterceptorChain(all);
	}

	private static class AsyncHttpClientRequestInterceptorAdapter implements AsyncHttpClientRequestInterceptor {

		private final HttpClientRequestInterceptor delegate;

		private AsyncHttpClientRequestInterceptorAdapter(HttpClientRequestInterceptor delegate) {
			this.delegate = delegate;
		}

		@Override
		public HttpClientRequest intercepts(HttpClientRequest request) {
			return delegate.intercepts(request);
		}

		@Override
		public AsyncHttpClientRequest interceptsAsync(AsyncHttpClientRequest request) {
			HttpClientRequest intercepted = delegate.intercepts(request);

			if (intercepted instanceof AsyncHttpClientRequest) {
				return (AsyncHttpClientRequest) intercepted;

			} else return new AsyncHttpClientRequestAdapter(intercepted);
		}
	}
}
