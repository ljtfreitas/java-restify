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
package com.github.ljtfreitas.restify.http.client.retry.async;

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.github.ljtfreitas.restify.http.client.retry.AlwaysRetryPolicy;
import com.github.ljtfreitas.restify.http.client.retry.BackOffPolicy;
import com.github.ljtfreitas.restify.http.client.retry.RetryConditionMatcher;
import com.github.ljtfreitas.restify.http.client.retry.RetryExhaustedException;
import com.github.ljtfreitas.restify.http.client.retry.RetryPolicy;

class AsyncRetryableLoop {

	private static final RetryPolicy ALWAYS_RETRY_POLICY = AlwaysRetryPolicy.instance();
	private static final BackOffPolicy DEFAULT_BACKOFF_POLICY = BackOffPolicy.instance();

	private final ScheduledExecutorService scheduler;
	private final RetryConditionMatcher retryConditionMatcher;
	private final BackOffPolicy backOffPolicy;
	private final RetryPolicy retryPolicy;

	AsyncRetryableLoop(ScheduledExecutorService scheduler, RetryConditionMatcher retryConditionMatcher) {
		this(scheduler, retryConditionMatcher, DEFAULT_BACKOFF_POLICY, ALWAYS_RETRY_POLICY);
	}

	AsyncRetryableLoop(ScheduledExecutorService scheduler, RetryConditionMatcher retryConditionMatcher, BackOffPolicy backOffPolicy) {
		this(scheduler, retryConditionMatcher, backOffPolicy, ALWAYS_RETRY_POLICY);
	}

	AsyncRetryableLoop(ScheduledExecutorService scheduler, RetryConditionMatcher retryConditionMatcher, BackOffPolicy backOffPolicy, RetryPolicy retryPolicy) {
		this.scheduler = nonNull(scheduler, "Scheduler Executor cannot be null");
		this.retryConditionMatcher = nonNull(retryConditionMatcher, "Retry condition matcher cannot be null");
		this.backOffPolicy = nonNull(backOffPolicy, "BackOff policy cannot be null");
		this.retryPolicy = nonNull(retryPolicy, "Retry policy cannot be null");
	}

	public <T> CompletableFuture<T> repeat(int attempts, AsyncRetryable<T> retryable) throws RetryExhaustedException {
		try {
			return new AsyncRetryLoop(attempts, retryPolicy.refresh(), backOffPolicy).repeat(retryable);

		} catch (RuntimeException e) {
			throw e;

		} catch (Exception e) {
			throw new RetryExhaustedException(e);
		}
	}

	private class AsyncRetryLoop {

		private final int attempts;
		private final RetryPolicy retryPolicy;
		private final BackOffPolicy backOffPolicy;

		private AsyncRetryLoop(int attempts, RetryPolicy retryPolicy, BackOffPolicy backOffPolicy) {
			this.attempts = attempts;
			this.retryPolicy = retryPolicy;
			this.backOffPolicy = backOffPolicy;
		}

		private <T> CompletableFuture<T> repeat(AsyncRetryable<T> retryable) throws Exception {
			CompletableFuture<T> retryableCompletableFuture = new CompletableFuture<>();

			scheduler.schedule(new AsyncRetryableRunnable<>(retryable, new AsyncRetryableExecution<>(1, retryableCompletableFuture)), 0, TimeUnit.MILLISECONDS);

			return retryableCompletableFuture;
		}

		private class AsyncRetryableExecution<T> {

			private final int attempt;
			private final CompletableFuture<T> response;

			private AsyncRetryableExecution(int attempt, CompletableFuture<T> response) {
				this.attempt = attempt;
				this.response = response;
			}

			private int attempt() {
				return attempt;
			}

			private int nextAttempt() {
				return attempt + 1;
			}

			private boolean retryable() {
				return nextAttempt() <= attempts;
			}

			private boolean retryable(Throwable exception) {
				return retryConditionMatcher.match(exception);
			}

			private void fail(Throwable cause) {
				response.completeExceptionally(new RetryExhaustedException("Exhausted with " + attempt + " attempts.", cause));
			}

			private void success(T result) {
				response.complete(result);
			}

			private void retry(AsyncRetryable<T> retryable) {
				AsyncRetryableExecution<T> nextExecution = new AsyncRetryableExecution<>(nextAttempt(), response);

				Duration backOff = backOffPolicy.backOff(nextExecution.attempt());

				scheduler.schedule(new AsyncRetryableRunnable<>(retryable, nextExecution), backOff.toMillis(), TimeUnit.MILLISECONDS);
			}
		}

		private class AsyncRetryableRunnable<T> implements Runnable {

			private final AsyncRetryable<T> retryable;
			private final AsyncRetryableExecution<T> execution;

			private AsyncRetryableRunnable(AsyncRetryable<T> retryable, AsyncRetryableExecution<T> execution) {
				this.retryable = retryable;
				this.execution = execution;
			}

			@Override
			public void run() {
				Semaphore lock = new Semaphore(1);

				try {
					lock.acquire();

					retryable.execute().whenComplete((result, throwable) -> {
						try {
							if (throwable != null) {
								Throwable cause = deepCause(throwable);

								if (execution.retryable() && execution.retryable(cause) && retryPolicy.retryable()) {
									execution.retry(retryable);

								} else {
									execution.fail(cause);
								}

							} else {
								execution.success(result);
							}

						} finally {
							lock.release();
						}
					});

				} catch (Exception e) {
					execution.fail(e);

				} finally {
					lock.release();
				}
			}
		}

		private Throwable deepCause(Throwable throwable) {
			return (throwable instanceof CompletionException || throwable instanceof ExecutionException) ?
					throwable.getCause() :
						throwable;
		}
	}
}
