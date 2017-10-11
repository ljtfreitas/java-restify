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
import java.util.EnumSet;
import java.util.function.Predicate;

import com.github.ljtfreitas.restify.http.client.response.HttpStatusCode;

public interface RetryCondition {

	public interface HttpStatusRetryCondition extends RetryCondition, Predicate<HttpStatusCode> {

		public static HttpStatusRetryCondition any4xx() {
			return any(HttpStatusCode.CLIENT_ERROR_STATUSES);
		}

		public static HttpStatusRetryCondition any5xx() {
			return any(HttpStatusCode.SERVER_ERROR_STATUSES);
		}

		public static HttpStatusRetryCondition any(HttpStatusCode... statuses) {
			return any(Arrays.asList(statuses));
		}

		public static HttpStatusRetryCondition any(Collection<HttpStatusCode> statuses) {
			EnumSet<HttpStatusCode> all = EnumSet.copyOf(statuses);
			return (s) -> all.contains(s);
		}
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
