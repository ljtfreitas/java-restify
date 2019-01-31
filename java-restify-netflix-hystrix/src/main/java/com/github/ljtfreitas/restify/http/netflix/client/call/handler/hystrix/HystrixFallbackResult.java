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

import java.util.concurrent.Future;

import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.FallbackResult;
import com.github.ljtfreitas.restify.util.Try;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;

class HystrixFallbackResult<T> implements FallbackResult<T> {

	private final FallbackResult<T> delegate;

	HystrixFallbackResult(FallbackResult<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T get() {
		T value = delegate.get();

		if (value instanceof HystrixCommand) {
			return asCommand(value).execute();

		} else if (value instanceof HystrixObservableCommand) {
			return asObservableCommand(value).toObservable().toBlocking().singleOrDefault(null);

		} else if (value instanceof Future) {
			return Try.of(() -> asFuture(value).get()).get();

		} else if (value instanceof Observable) {
			return asObservable(value).toBlocking().singleOrDefault(null);

		} else return value;
	}


	@SuppressWarnings("unchecked")
	private HystrixCommand<T> asCommand(T value) {
		return (HystrixCommand<T>) value;
	}

	@SuppressWarnings("unchecked")
	private HystrixObservableCommand<T> asObservableCommand(T value) {
		return (HystrixObservableCommand<T>) value;
	}

	@SuppressWarnings("unchecked")
	private Future<T> asFuture(T value) {
		return (Future<T>) value;
	}

	@SuppressWarnings("unchecked")
	private Observable<T> asObservable(T value) {
		return (Observable<T>) value;
	}
}