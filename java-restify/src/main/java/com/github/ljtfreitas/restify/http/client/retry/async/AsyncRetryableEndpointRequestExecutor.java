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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.retry.BackOffPolicy;
import com.github.ljtfreitas.restify.http.client.retry.BackOffPolicyFactory;
import com.github.ljtfreitas.restify.http.client.retry.RetryConditionMatcher;
import com.github.ljtfreitas.restify.http.client.retry.RetryConfiguration;
import com.github.ljtfreitas.restify.http.client.retry.RetryConfigurationFactory;
import com.github.ljtfreitas.restify.http.client.retry.RetryPolicy;
import com.github.ljtfreitas.restify.http.client.retry.RetryPolicyFactory;
import com.github.ljtfreitas.restify.http.client.retry.RetryableEndpointRequestExecutor;

public class AsyncRetryableEndpointRequestExecutor implements AsyncEndpointRequestExecutor {

	private final AsyncEndpointRequestExecutor asyncEndpointRequestExecutor;
	private final ScheduledExecutorService scheduler;
	private final RetryConfigurationFactory retryConfigurationFactory;
	private final RetryableEndpointRequestExecutor delegate;

	public AsyncRetryableEndpointRequestExecutor(AsyncEndpointRequestExecutor asyncEndpointRequestExecutor,
			ScheduledExecutorService scheduledExecutorService) {
		this(asyncEndpointRequestExecutor, scheduledExecutorService, null);
	}

	public AsyncRetryableEndpointRequestExecutor(AsyncEndpointRequestExecutor asyncEndpointRequestExecutor,
			ScheduledExecutorService scheduledExecutorService, RetryConfiguration configuration) {
		this(asyncEndpointRequestExecutor, scheduledExecutorService, configuration,
				new RetryableEndpointRequestExecutor(asyncEndpointRequestExecutor, configuration));
	}

	public AsyncRetryableEndpointRequestExecutor(AsyncEndpointRequestExecutor asyncEndpointRequestExecutor,
			ScheduledExecutorService scheduledExecutorService, RetryConfiguration configuration, RetryableEndpointRequestExecutor delegate) {
		this.asyncEndpointRequestExecutor = asyncEndpointRequestExecutor;
		this.scheduler = scheduledExecutorService;
		this.retryConfigurationFactory = new RetryConfigurationFactory(configuration);
		this.delegate = delegate;
	}

	@Override
	public <T> EndpointResponse<T> execute(EndpointRequest endpointRequest) {
		return delegate.execute(endpointRequest);
	}

	@Override
	public <T> CompletableFuture<EndpointResponse<T>> executeAsync(EndpointRequest endpointRequest) {
		RetryConfiguration retryConfiguration = retryConfigurationFactory.createOf(endpointRequest);

		RetryPolicy retryPolicy = new RetryPolicyFactory(retryConfiguration).create();

		BackOffPolicy backOffPolicy = new BackOffPolicyFactory(retryConfiguration).create();

		AsyncRetryableLoop retryLoop = new AsyncRetryableLoop(scheduler, new RetryConditionMatcher(retryConfiguration.conditions()),
				backOffPolicy, retryPolicy);

		return retryLoop.repeat(retryConfiguration.attempts(), () -> asyncEndpointRequestExecutor.executeAsync(endpointRequest));
	}
}
