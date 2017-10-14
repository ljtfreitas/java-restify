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

import com.github.ljtfreitas.restify.http.RestifyHttpException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseException;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.StatusCodeRetryCondition;
import com.github.ljtfreitas.restify.http.client.retry.RetryCondition.ThrowableRetryCondition;

class RetryConditionMatcher {

	private final Collection<StatusCodeRetryCondition> statusCodeConditions;
	private final Collection<ThrowableRetryCondition> throwableConditions;

	public RetryConditionMatcher(Collection<RetryCondition> conditions) {
		this.statusCodeConditions = conditions.stream().filter(c -> c instanceof StatusCodeRetryCondition)
				.map(c -> (StatusCodeRetryCondition) c).collect(Collectors.toList());
		this.throwableConditions = conditions.stream().filter(c -> c instanceof ThrowableRetryCondition)
				.map(c -> (ThrowableRetryCondition) c).collect(Collectors.toList());
	}

	public boolean match(Throwable throwable) {
		if (throwable instanceof RestifyEndpointResponseException) {
			RestifyEndpointResponseException response = (RestifyEndpointResponseException) throwable;
			return doMatch(response.status()) || exausth(throwable);

		} else if (throwable instanceof RestifyHttpException) {
			return exausth(throwable.getCause());

		} else {
			return exausth(throwable);
		}
	}

	private boolean exausth(Throwable throwable) {
		return throwable == null ? false : doMatch(throwable) || exausth(throwable.getCause());
	}

	private boolean doMatch(Throwable throwable) {
		return throwableConditions.stream().anyMatch(c -> c.test(throwable));
	}

	private boolean doMatch(StatusCode status) {
		return statusCodeConditions.stream().anyMatch(c -> c.test(status));
	}
}
