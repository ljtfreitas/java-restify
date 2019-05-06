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
package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.CIRCUIT_BREAKER_ENABLED;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.CIRCUIT_BREAKER_FORCE_CLOSED;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.CIRCUIT_BREAKER_FORCE_OPEN;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_STRATEGY;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_CANCEL;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_TIMEOUT;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_TIMEOUT_ENABLED;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.FALLBACK_ENABLED;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.FALLBACK_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.METRICS_HEALTH_SNAPSHOT_INTERVAL_IN_MILLISECONDS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.METRICS_ROLLING_PERCENTILE_BUCKET_SIZE;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.METRICS_ROLLING_PERCENTILE_ENABLED;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.METRICS_ROLLING_PERCENTILE_NUM_BUCKETS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.METRICS_ROLLING_PERCENTILE_TIME_IN_MILLISECONDS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.METRICS_ROLLING_STATS_NUM_BUCKETS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;

class HystrixCommandPropertiesWriter {

	private static final Map<String, HystrixPropertyWriter<HystrixCommandProperties.Setter>> HYSTRIX_COMMAND_PROPERTY_WRITERS
		= new LinkedHashMap<>();

	private final Map<String, String> properties;

	HystrixCommandPropertiesWriter(Map<String, String> properties) {
		this.properties = properties;
	}

	void applyTo(HystrixCommandProperties.Setter hystrixCommandProperties) {
		properties.entrySet().stream()
			.forEach(property -> Optional.ofNullable(HYSTRIX_COMMAND_PROPERTY_WRITERS.get(property.getKey()))
					.ifPresent(s -> s.set(hystrixCommandProperties, property.getValue())));
	}

	static {
		// Execution
		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(EXECUTION_ISOLATION_STRATEGY,
				(s, v) -> s.withExecutionIsolationStrategy(ExecutionIsolationStrategy.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS,
				(s, v) -> s.withExecutionTimeoutInMilliseconds(Integer.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(EXECUTION_TIMEOUT_ENABLED,
				(s, v) -> s.withExecutionTimeoutEnabled(Boolean.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_TIMEOUT,
				(s, v) -> s.withExecutionIsolationThreadInterruptOnTimeout(Boolean.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_CANCEL,
				(s, v) -> s.withExecutionIsolationThreadInterruptOnFutureCancel(Boolean.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS,
				(s, v) -> s.withExecutionIsolationSemaphoreMaxConcurrentRequests(Integer.valueOf(v)));

		// Fallback
		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(FALLBACK_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS,
				(s, v) -> s.withFallbackIsolationSemaphoreMaxConcurrentRequests(Integer.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(FALLBACK_ENABLED,
				(s, v) -> s.withFallbackEnabled(Boolean.valueOf(v)));

		// Circuit breaker
		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(CIRCUIT_BREAKER_ENABLED,
				(s, v) -> s.withCircuitBreakerEnabled(Boolean.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD,
				(s, v) -> s.withCircuitBreakerRequestVolumeThreshold(Integer.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS,
				(s, v) -> s.withCircuitBreakerSleepWindowInMilliseconds(Integer.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE,
				(s, v) -> s.withCircuitBreakerErrorThresholdPercentage(Integer.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(CIRCUIT_BREAKER_FORCE_OPEN,
				(s, v) -> s.withCircuitBreakerForceOpen(Boolean.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(CIRCUIT_BREAKER_FORCE_CLOSED,
				(s, v) -> s.withCircuitBreakerForceClosed(Boolean.valueOf(v)));

		// Metrics
		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS,
				(s, v) -> s.withMetricsRollingStatisticalWindowInMilliseconds(Integer.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(METRICS_ROLLING_STATS_NUM_BUCKETS,
				(s, v) -> s.withMetricsRollingStatisticalWindowBuckets(Integer.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(METRICS_ROLLING_PERCENTILE_ENABLED,
				(s, v) -> s.withMetricsRollingPercentileEnabled(Boolean.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(METRICS_ROLLING_PERCENTILE_TIME_IN_MILLISECONDS,
				(s, v) -> s.withMetricsRollingPercentileWindowInMilliseconds(Integer.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(METRICS_ROLLING_PERCENTILE_NUM_BUCKETS,
				(s, v) -> s.withMetricsRollingPercentileWindowBuckets(Integer.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(METRICS_ROLLING_PERCENTILE_BUCKET_SIZE,
				(s, v) -> s.withMetricsRollingPercentileBucketSize(Integer.valueOf(v)));

		HYSTRIX_COMMAND_PROPERTY_WRITERS.put(METRICS_HEALTH_SNAPSHOT_INTERVAL_IN_MILLISECONDS,
				(s, v) -> s.withMetricsHealthSnapshotIntervalInMilliseconds(Integer.valueOf(v)));
	}
}
