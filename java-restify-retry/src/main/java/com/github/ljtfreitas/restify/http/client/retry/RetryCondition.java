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
package com.github.ljtfreitas.restify.http.client.retry;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

public interface RetryCondition {

	public interface StatusCodeRetryCondition extends RetryCondition, Predicate<StatusCode> {

		public static StatusCodeRetryCondition any4xx() {
			return (s) -> s.isClientError();
		}

		public static StatusCodeRetryCondition any5xx() {
			return (s) -> s.isServerError();
		}

		public static StatusCodeRetryCondition any(HttpStatusCode... statuses) {
			Collection<StatusCode> collection = Arrays.asList(statuses).stream().map(StatusCode::of).collect(Collectors.toSet());
			return (s) -> collection.contains(s);
		}

		public static StatusCodeRetryCondition any(StatusCode... statuses) {
			Collection<StatusCode> collection = Arrays.asList(statuses).stream().collect(Collectors.toSet());
			return (s) -> collection.contains(s);
		}
	}

	public interface HeadersRetryCondition extends RetryCondition, Predicate<Headers> {

		public static HeadersRetryCondition contains(Header header) {
			return (headers) -> headers.get(header.name()).filter(h -> h.equals(header)).isPresent();
		}

		public static HeadersRetryCondition contains(String header) {
			return (headers) -> headers.get(header).isPresent();
		}
	}

	public interface EndpointResponseRetryCondition extends RetryCondition, Predicate<EndpointResponse<String>> {
	}

	public interface ThrowableRetryCondition extends RetryCondition, Predicate<Throwable> {

		public static ThrowableRetryCondition ioFailure() {
			return any(IOException.class);
		}

		@SafeVarargs
		public static ThrowableRetryCondition any(Class<? extends Throwable>... throwables) {
			return any(Arrays.asList(throwables));
		}

		public static ThrowableRetryCondition any(Collection<Class<? extends Throwable>> throwables) {
			return (t) -> t == null ? false
					: throwables.stream().anyMatch(a -> a.isInstance(t)) || any(throwables).test(t.getCause());
		}
	}
}
