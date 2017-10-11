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

import static com.github.ljtfreitas.restify.http.util.Preconditions.nonNull;

class RetryableLoop {

	private static final RetryPolicy ALWAYS_RETRY_POLICY = new AlwaysRetryPolicy();

	private final RetryConditionMatcher retryExceptionPolicy;
	private final RetryPolicy retryPolicy;

	RetryableLoop(RetryConditionMatcher retryExceptionPolicy) {
		this(retryExceptionPolicy, ALWAYS_RETRY_POLICY);
	}

	RetryableLoop(RetryConditionMatcher retryExceptionPolicy, RetryPolicy retryPolicy) {
		this.retryExceptionPolicy = nonNull(retryExceptionPolicy, "Retry exception policy cannot be null");
		this.retryPolicy = nonNull(retryPolicy, "Retry policy cannot be null");
	}

	public <T> T repeat(int attempts, Retryable<T> retryable) throws RetryExhaustedException {
		try {
			return new RetryLoop(attempts, retryPolicy.refresh()).repeat(retryable);

		} catch (RuntimeException e) {
			throw e;

		} catch (Exception e) {
			throw new RetryExhaustedException(e);
		}
	}

	private class RetryLoop {

		private final int attempts;
		private final RetryPolicy retryPolicy;

		private RetryLoop(int attempts, RetryPolicy retryPolicy) {
			this.attempts = attempts;
			this.retryPolicy = retryPolicy;
		}

		private <T> T repeat(Retryable<T> retryable) throws Exception {
			return doRepeat(0, retryable, null);
		}

		private <T> T doRepeat(int counter, Retryable<T> retryable, Exception last) throws Exception {
			Exception current = last;

			while (retryable(++counter) && retryPolicy.retryable()) {
				try {
					return retryable.execute();
				} catch (Exception e) {
					current = e;
					if (retryable(current)) return doRepeat(counter, retryable, current); else throw current;
				}
			}

			if (current == null) throw new RetryExhaustedException(); throw current;
		}

		private boolean retryable(int counter) {
			return (counter) <= attempts;
		}

		private boolean retryable(Exception exception) {
			return retryExceptionPolicy.match(exception);
		}
	}
}
