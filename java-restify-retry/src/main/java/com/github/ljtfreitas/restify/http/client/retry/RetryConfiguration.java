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

import static com.github.ljtfreitas.restify.http.client.retry.RetryCondition.StatusCodeRetryCondition.any;
import static com.github.ljtfreitas.restify.http.client.retry.RetryCondition.ThrowableRetryCondition.any;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.EndpointResponseRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.HeadersRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.StatusCodeRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.ThrowableRetryCondition;

public class RetryConfiguration {

	public static final int MINIMUM_ATTEMPTS = 1;
	public static final Duration UNDEFINED_TIMEOUT = Duration.ZERO;

	private int attempts = MINIMUM_ATTEMPTS;
	private Duration timeout = UNDEFINED_TIMEOUT;

	private BackOff backOff = new BackOff();

	private Collection<RetryCondition> conditions = new ArrayList<>();

	private RetryConfiguration() {
	}

	private RetryConfiguration(RetryConfiguration source) {
		this.attempts = source.attempts;
		this.timeout = source.timeout;
		this.backOff = new BackOff(source.backOff);
		this.conditions = new ArrayList<>(source.conditions);
	}

	public int attempts() {
		return attempts;
	}

	public Duration timeout() {
		return timeout;
	}

	public BackOff backOff() {
		return backOff;
	}

	public Collection<RetryCondition> conditions() {
		return Collections.unmodifiableCollection(conditions);
	}

	public static RetryConfiguration simple() {
		RetryConfiguration configuration = new RetryConfiguration();
		configuration.attempts = MINIMUM_ATTEMPTS;
		return configuration;
	}

	public class BackOff {

		private Duration delay = Duration.ofMillis(BackOffPolicy.DEFAULT_BACKOFF_DELAY);
		private double multiplier = BackOffPolicy.DEFAULT_BACKOFF_MULTIPLIER;

		private BackOff() {
		}

		private BackOff(BackOff source) {
			this.delay = source.delay;
			this.multiplier = source.multiplier;
		}

		public Duration delay() {
			return delay;
		}

		public double multiplier() {
			return multiplier;
		}
	}

	public static class Builder {

		private final RetryConfiguration configuration = new RetryConfiguration();
		private final RetryBackOffBuilder backOff = new RetryBackOffBuilder();

		public Builder attempts(int attempts) {
			configuration.attempts = (attempts <= 0) ? MINIMUM_ATTEMPTS : attempts;
			return this;
		}

		public Builder timeout(long timeout) {
			configuration.timeout = (timeout <= 0) ? UNDEFINED_TIMEOUT : Duration.ofMillis(timeout);
			return this;
		}

		public Builder timeout(Duration timeout) {
			configuration.timeout = UNDEFINED_TIMEOUT.compareTo(timeout) <= 0 ? UNDEFINED_TIMEOUT : timeout;
			return this;
		}

		public RetryBackOffBuilder backOff() {
			return backOff;
		}

		public Builder when(HttpStatusCode... statuses) {
			configuration.conditions.add(any(statuses));
			return this;
		}

		public Builder when(StatusCodeRetryCondition condition) {
			configuration.conditions.add(condition);
			return this;
		}

		@SafeVarargs
		public final Builder when(Class<? extends Throwable>... throwableTypes) {
			configuration.conditions.add(any(throwableTypes));
			return this;
		}

		public final Builder when(ThrowableRetryCondition condition) {
			configuration.conditions.add(condition);
			return this;
		}

		public final Builder when(HeadersRetryCondition condition) {
			configuration.conditions.add(condition);
			return this;
		}

		public final Builder when(EndpointResponseRetryCondition condition) {
			configuration.conditions.add(condition);
			return this;
		}

		public RetryConfiguration build() {
			return new RetryConfiguration(configuration);
		}

		public class RetryBackOffBuilder {

			public RetryBackOffBuilder delay(long delay) {
				configuration.backOff.delay = Duration.ofMillis(delay);
				return this;
			}

			public RetryBackOffBuilder delay(Duration delay) {
				configuration.backOff.delay = delay;
				return this;
			}

			public RetryBackOffBuilder multiplier(double multiplier) {
				configuration.backOff.multiplier = multiplier;
				return this;
			}

			public Builder and() {
				return RetryConfiguration.Builder.this;
			}
		}
	}
}
