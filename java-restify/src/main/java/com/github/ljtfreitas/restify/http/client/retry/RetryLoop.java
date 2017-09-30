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

import static com.github.ljtfreitas.restify.http.util.Preconditions.isTrue;
import static com.github.ljtfreitas.restify.http.util.Preconditions.nonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class RetryLoop {

	private static final RetryPolicy ALWAYS_RETRY_POLICY = new AlwaysRetryPolicy();

	private final int attempts;
	private final Set<Class<? extends Exception>> exceptions;
	private final RetryPolicy retryPolicy;

	private int counter = 0;

	public RetryLoop(int attempts, Collection<Class<? extends Exception>> exceptions) {
		this(attempts, exceptions, ALWAYS_RETRY_POLICY);
	}

	public RetryLoop(int attempts, Collection<Class<? extends Exception>> exceptions, RetryPolicy retryPolicy) {
		isTrue(attempts >= 1, "Number of attempts must be at least 1");
		this.attempts = attempts;
		this.retryPolicy = nonNull(retryPolicy, "RetryPolicy cannot be null.");
		this.exceptions = new HashSet<>(nonNull(exceptions, "Collection of exceptions cannot be null."));
	}

	public <T> T repeat(Retryable<T> retryable) throws RuntimeException {
		try {
			this.counter = 0;

			return doRepeat(retryable, null);

		} catch (RuntimeException e) {
			throw e;

		} catch (Exception e) {
			throw new RetryExhaustedException(e);
		}
	}

	private <T> T doRepeat(Retryable<T> retryable, Exception last) throws Exception {
		Exception current = last;

		while (retryable() && retryPolicy.retryable()) {
			try {
				return retryable.execute();
			} catch (Exception e) {
				current = e;
				if (retryable(current)) return doRepeat(retryable, current); else throw current;
			}
		}

		if (current == null) throw new RetryExhaustedException(); throw current;
	}

	private boolean retryable() {
		return (++this.counter) <= attempts;
	}

	private boolean retryable(Exception exception) {
		return exceptions.contains(exception.getClass());
	}
}
