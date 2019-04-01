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

import java.time.Duration;
import java.util.stream.LongStream;

public class BackOffPolicy {

	private static final BackOffPolicy DEFAULT_BACKOFF_POLICY = new BackOffPolicy();

	public static final long DEFAULT_BACKOFF_DELAY = 1000l;
	public static final double DEFAULT_BACKOFF_MULTIPLIER = 1d;

	private final long delay;
	private final double multiplier;

	public BackOffPolicy() {
		this(DEFAULT_BACKOFF_DELAY, DEFAULT_BACKOFF_MULTIPLIER);
	}

	public BackOffPolicy(long delay) {
		this(delay, DEFAULT_BACKOFF_MULTIPLIER);
	}

	public BackOffPolicy(long delay, double multiplier) {
		this.delay = delay;
		this.multiplier = multiplier;
	}

	public Duration backOff(int attempt) {
		long duration = attempt == 1 ? delay : calculateTo(attempt);

		return Duration.ofMillis(duration);
	}

	private long calculateTo(int attempt) {
		ExponentialBackOffContext context = new ExponentialBackOffContext(delay, multiplier);

		return LongStream.generate(context::next).limit(attempt).max().getAsLong();
	}

	private class ExponentialBackOffContext {

		private final long delay;
		private final double multiplier;

		private long attempt = 0;

		public ExponentialBackOffContext(long delay, double multiplier) {
			this.delay = delay;
			this.multiplier = multiplier;
		}

		private long next() {
			attempt = (long) (attempt == 0 ? delay : attempt * multiplier);
			return attempt;
		}
	}

	public static BackOffPolicy instance() {
		return DEFAULT_BACKOFF_POLICY;
	}
}
