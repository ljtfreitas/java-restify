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

import static com.github.ljtfreitas.restify.http.client.retry.RetryCondition.HttpStatusRetryCondition.any;
import static com.github.ljtfreitas.restify.http.client.retry.RetryCondition.ThrowableRetryCondition.any;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.HttpStatusRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.ThrowableRetryCondition;

public class RetryConfiguration {

	public static final Integer MINIMUM_ATTEMPTS = 1;
	public static final Integer UNDEFINED_TIMEOUT = null;

	private Integer attempts = MINIMUM_ATTEMPTS;
	private Integer timeout = UNDEFINED_TIMEOUT;

	private Collection<HttpStatusRetryCondition> httpStatusConditions = new ArrayList<>();
	private Collection<ThrowableRetryCondition> throwableConditions = new ArrayList<>();

	public Optional<Integer> attempts() {
		return Optional.ofNullable(attempts);
	}

	public Optional<Integer> timeout() {
		return Optional.ofNullable(timeout);
	}

	public boolean retryable(HttpStatusCode status) {
		return httpStatusConditions.stream().anyMatch(condition -> condition.test(status));
	}

	public boolean retryable(Throwable throwable) {
		return throwableConditions.stream().anyMatch(condition -> condition.test(throwable));
	}

	public static RetryConfiguration minimum() {
		RetryConfiguration configuration = new RetryConfiguration();
		configuration.attempts = MINIMUM_ATTEMPTS;
		return configuration;
	}

	public static class Builder {

		private RetryConfiguration configuration = new RetryConfiguration();

		public Builder attempts(int attempts) {
			configuration.attempts = (attempts <= 0) ? MINIMUM_ATTEMPTS : attempts;
			return this;
		}

		public Builder timeout(int timeout) {
			configuration.timeout = (timeout <= 0) ? UNDEFINED_TIMEOUT : timeout;
			return this;
		}

		public Builder when(HttpStatusCode... statuses) {
			configuration.httpStatusConditions.add(any(statuses));
			return this;
		}

		public Builder when(HttpStatusRetryCondition condition) {
			configuration.httpStatusConditions.add(condition);
			return this;
		}

		@SafeVarargs
		public final Builder when(Class<? extends Throwable>... throwableTypes) {
			configuration.throwableConditions.add(any(throwableTypes));
			return this;
		}

		public final Builder when(ThrowableRetryCondition condition) {
			configuration.throwableConditions.add(condition);
			return this;
		}

		public RetryConfiguration build() {
			return configuration;
		}
	}
}
