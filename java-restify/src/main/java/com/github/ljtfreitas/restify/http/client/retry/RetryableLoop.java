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

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import com.github.ljtfreitas.restify.util.Tryable;

class RetryableLoop {

	private static final RetryPolicy ALWAYS_RETRY_POLICY = AlwaysRetryPolicy.instance();
	private static final BackOffPolicy DEFAULT_BACKOFF_POLICY = BackOffPolicy.instance();

	private final RetryConditionMatcher retryConditionMatcher;
	private final BackOffPolicy backOffPolicy;
	private final RetryPolicy retryPolicy;

	RetryableLoop(RetryConditionMatcher retryConditionMatcher) {
		this(retryConditionMatcher, DEFAULT_BACKOFF_POLICY, ALWAYS_RETRY_POLICY);
	}

	RetryableLoop(RetryConditionMatcher retryConditionMatcher, BackOffPolicy backOffPolicy) {
		this(retryConditionMatcher, backOffPolicy, ALWAYS_RETRY_POLICY);
	}

	RetryableLoop(RetryConditionMatcher retryConditionMatcher, BackOffPolicy backOffPolicy, RetryPolicy retryPolicy) {
		this.retryConditionMatcher = nonNull(retryConditionMatcher, "Retry condition matcher cannot be null");
		this.backOffPolicy = nonNull(backOffPolicy, "BackOff policy cannot be null");
		this.retryPolicy = nonNull(retryPolicy, "Retry policy cannot be null");
	}

	public <T> T repeat(int attempts, Retryable<T> retryable) throws RetryExhaustedException {
		try {
			return new RetryLoop(attempts, retryPolicy.refresh(), backOffPolicy).repeat(retryable);

		} catch (RuntimeException e) {
			throw e;

		} catch (Exception e) {
			throw new RetryExhaustedException(e);
		}
	}

	private class RetryLoop {

		private final int attempts;
		private final RetryPolicy retryPolicy;
		private final BackOffPolicy backOffPolicy;

		private RetryLoop(int attempts, RetryPolicy retryPolicy, BackOffPolicy backOffPolicy) {
			this.attempts = attempts;
			this.retryPolicy = retryPolicy;
			this.backOffPolicy = backOffPolicy;
		}

		private <T> T repeat(Retryable<T> retryable) throws Exception {
			return doRepeat(0, retryable, null);
		}

		private <T> T doRepeat(int attempt, Retryable<T> retryable, Exception last) throws Exception {
			Exception current = last;

			while (retryable(++attempt) && retryPolicy.retryable()) {
				try {
					return retryable.execute();
				} catch (Exception e) {
					current = e;
					if (retryable(current)) return retry(attempt, retryable, current); else throw current;
				}
			}

			if (current == null) throw new RetryExhaustedException("Exhausted with " + attempt + " attempts."); throw current;
		}

		private <T> T retry(int attempt, Retryable<T> retryable, Exception last) throws Exception {
			Tryable.run(() -> Thread.sleep(backOffPolicy.backOff(attempt).toMillis()));
			return doRepeat(attempt, retryable, last);
		}

		private boolean retryable(int counter) {
			return (counter) <= attempts;
		}

		private boolean retryable(Exception exception) {
			return retryConditionMatcher.match(exception);
		}
	}
}
