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

import java.util.Collection;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseException;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.EndpointResponseRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.HeadersRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.StatusCodeRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.ThrowableRetryCondition;

public class RetryConditionMatcher {

	private final Collection<StatusCodeRetryCondition> statusCodeConditions;
	private final Collection<ThrowableRetryCondition> throwableConditions;
	private final Collection<HeadersRetryCondition> headersConditions;
	private final Collection<EndpointResponseRetryCondition> endpointResponseConditions;

	public RetryConditionMatcher(Collection<RetryCondition> conditions) {
		this.statusCodeConditions = conditions.stream().filter(c -> c instanceof StatusCodeRetryCondition)
				.map(c -> (StatusCodeRetryCondition) c).collect(Collectors.toList());
		this.throwableConditions = conditions.stream().filter(c -> c instanceof ThrowableRetryCondition)
				.map(c -> (ThrowableRetryCondition) c).collect(Collectors.toList());
		this.headersConditions = conditions.stream().filter(c -> c instanceof HeadersRetryCondition)
				.map(c -> (HeadersRetryCondition) c).collect(Collectors.toList());
		this.endpointResponseConditions = conditions.stream().filter(c -> c instanceof EndpointResponseRetryCondition)
				.map(c -> (EndpointResponseRetryCondition) c).collect(Collectors.toList());
	}

	public boolean match(Throwable throwable) {
		if (throwable instanceof EndpointResponseException) {
			EndpointResponseException exception = (EndpointResponseException) throwable;
			return doMatch(exception.status()) || doMatch(exception.headers()) || doMatch(exception.response());
		} else {
			return doMatch(throwable);
		}
	}

	private boolean doMatch(Throwable throwable) {
		return throwableConditions.stream().anyMatch(c -> c.test(throwable));
	}

	private boolean doMatch(StatusCode status) {
		return statusCodeConditions.stream().anyMatch(c -> c.test(status));
	}

	private boolean doMatch(Headers headers) {
		return headersConditions.stream().anyMatch(c -> c.test(headers));
	}

	private boolean doMatch(EndpointResponse<String> response) {
		return endpointResponseConditions.stream().anyMatch(c -> c.test(response));
	}
}
