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

import static com.github.ljtfreitas.restify.http.client.retry.RetryConfiguration.MINIMUM_ATTEMPTS;

import java.time.Duration;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

public class RetryableEndpointRequestExecutor implements EndpointRequestExecutor {

	private final EndpointRequestExecutor delegate;
	private final RetryConfiguration configuration;

	public RetryableEndpointRequestExecutor(EndpointRequestExecutor delegate) {
		this(delegate, null);
	}

	public RetryableEndpointRequestExecutor(EndpointRequestExecutor delegate, RetryConfiguration configuration) {
		this.delegate = delegate;
		this.configuration = configuration;
	}

	@Override
	public <T> EndpointResponse<T> execute(EndpointRequest endpointRequest) {
		RetryConfiguration configuration = configurationOf(endpointRequest);

		RetryableLoop retryLoop = new RetryableLoop(new RetryConditionMatcher(configuration), retryPolicyOf(configuration));

		return retryLoop.repeat(configuration.attempts().orElse(MINIMUM_ATTEMPTS), () -> delegate.execute(endpointRequest));
	}

	private RetryConfiguration configurationOf(EndpointRequest endpointRequest) {
		return endpointRequest.metadata().get(Retry.class)
				.map(retry -> configurationOf(retry))
					.orElseGet(() -> Optional.ofNullable(configuration).orElseGet(RetryConfiguration::minimum));
	}

	private RetryConfiguration configurationOf(Retry retry) {
		return new RetryConfiguration.Builder()
				.attempts(retry.attempts())
				.timeout(retry.timeout())
				.build();
	}

	private RetryPolicy retryPolicyOf(RetryConfiguration configuration) {
		if (configuration.timeout().isPresent()) {
			return new TimeoutRetryPolicy(Duration.ofMillis(configuration.timeout().get()));

		} else return null;
	}
}
