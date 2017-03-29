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
package com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CircuitBreakerProperty {

	String name();

	String value();

	// Execution
	public static final String EXECUTION_ISOLATION_STRATEGY = "execution.isolation.strategy";
	public static final String EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS = "execution.isolation.thread.timeoutInMilliseconds";
	public static final String EXECUTION_TIMEOUT_ENABLED = "execution.timeout.enabled";
	public static final String EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_TIMEOUT = "execution.isolation.thread.interruptOnTimeout";
	public static final String EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_CANCEL = "execution.isolation.thread.interruptOnCancel";
	public static final String EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS = "execution.isolation.semaphore.maxConcurrentRequests";

	// Fallback
	public static final String FALLBACK_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS = "fallback.isolation.semaphore.maxConcurrentRequests";
	public static final String FALLBACK_ENABLED = "fallback.enabled";

	// Circuit breaker
	public static final String CIRCUIT_BREAKER_ENABLED = "circuitBreaker.enabled";
	public static final String CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD = "circuitBreaker.requestVolumeThreshold";
	public static final String CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS = "circuitBreaker.sleepWindowInMilliseconds";
	public static final String CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE = "circuitBreaker.errorThresholdPercentage";
	public static final String CIRCUIT_BREAKER_FORCE_OPEN = "circuitBreaker.forceOpen";
	public static final String CIRCUIT_BREAKER_FORCE_CLOSED = "circuitBreaker.forceClosed";

	// Metrics
	public static final String METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS = "metrics.rollingStats.timeInMilliseconds";
	public static final String METRICS_ROLLING_STATS_NUM_BUCKETS = "metrics.rollingStats.numBuckets";
	public static final String METRICS_ROLLING_PERCENTILE_ENABLED = "metrics.rollingPercentile.enabled";
	public static final String METRICS_ROLLING_PERCENTILE_TIME_IN_MILLISECONDS = "metrics.rollingPercentile.timeInMilliseconds";
	public static final String METRICS_ROLLING_PERCENTILE_NUM_BUCKETS = "metrics.rollingPercentile.numBuckets";
	public static final String METRICS_ROLLING_PERCENTILE_BUCKET_SIZE = "metrics.rollingPercentile.bucketSize";
	public static final String METRICS_HEALTH_SNAPSHOT_INTERVAL_IN_MILLISECONDS = "metrics.healthSnapshot.intervalInMilliseconds";

	// Thread pool
	public static final String THREAD_POOL_MAX_QUEUE_SIZE = "threadpool.default.maxQueueSize";
	public static final String THREAD_POOL_CORE_SIZE = "threadpool.default.coreSize";
	public static final String THREAD_POOL_KEEP_ALIVE_TIME_MINUTES = "threadpool.default.keepAliveTimeMinutes";
	public static final String THREAD_POOL_QUEUE_SIZE_REJECTION_THRESHOLD = "threadpool.default.queueSizeRejectionThreshold";
	public static final String THREAD_POOL_ALLOW_MAXIMUM_SIZE_TO_DIVERGE_FROM_CORE_SIZE = "threadpool.default.allowMaximumSizeToDivergeFromCoreSize";
	public static final String THREAD_POOL_METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS = "threadpool.default.metrics.rollingStats.timeInMilliseconds";
	public static final String THREAD_POOL_METRICS_ROLLING_STATS_NUM_BUCKETS = "threadpool.default.metrics.rollingStats.numBuckets";
}
