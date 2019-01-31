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
package com.github.ljtfreitas.restify.http.client.response;

import java.util.function.Function;
import java.util.function.Predicate;

import com.github.ljtfreitas.restify.util.Try;

class FailureEndpointResponse<T> extends BaseEndpointResponse<T> {

	private final EndpointResponseException exception;

	FailureEndpointResponse(EndpointResponseException exception) {
		super(exception.status(), exception.headers());
		this.exception = exception;
	}

	@Override
	public T body() {
		throw exception;
	}

	@Override
	public EndpointResponse<T> recover(Function<EndpointResponseException, EndpointResponse<T>> mapper) {
		Try<EndpointResponse<T>> failure = Try.failure(exception);

		return failure
					.recover(e -> Try.of(() -> mapper.apply((EndpointResponseException) e)))
					.recover(EndpointResponseException.class, e -> Try.success(EndpointResponse.error(e)))
						.get();
	}
	
	@Override
	public EndpointResponse<T> recover(Predicate<EndpointResponseException> predicate,
			Function<EndpointResponseException, EndpointResponse<T>> mapper) {

		Try<EndpointResponse<T>> failure = Try.failure(exception);

		return failure
					.recover(e -> predicate.test((EndpointResponseException) e) ? Try.of(() -> mapper.apply((EndpointResponseException) e)) : failure)
					.recover(EndpointResponseException.class, e -> Try.success(EndpointResponse.error(e)))
						.get();
	}

	@Override
	public <E extends EndpointResponseException> EndpointResponse<T> recover(Class<? extends E> predicate,
			Function<E, EndpointResponse<T>> mapper) {

		Try<EndpointResponse<T>> failure = Try.failure(exception);

		return failure
					.recover(predicate, e -> Try.of(() -> mapper.apply((E) e)))
					.recover(EndpointResponseException.class, e -> Try.success(EndpointResponse.error(e)))
						.get();
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("FailureEndpointResponse: [")
				.append("HTTP Status code: ")
					.append(status())
				.append(", ")
				.append("Headers: ")
					.append(headers())
				.append(", ")
				.append("Body: ")
					.append(exception.bodyAsString())
			.append("]");

		return report.toString();
	}
}
