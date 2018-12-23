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

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;

class ObservableFallbackResult implements FallbackResult<Observable<Object>> {

	private final FallbackResult<Object> delegate;

	ObservableFallbackResult(FallbackResult<Object> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Observable<Object> get() throws Exception {
		Object value = delegate.get();

		if (value instanceof HystrixCommand) {
			return asCommand(value).observe();

		} else if (value instanceof HystrixObservableCommand) {
			return asObservableCommand(value).observe();

		} else if (value instanceof Future) {
			return Observable.from(asFuture(value));

		} else if (value instanceof Observable) {
			return asObservable(value);

		} else if (value == null) {
			return Observable.empty();

		} else return Observable.just(value);
	}


	@SuppressWarnings("unchecked")
	private HystrixCommand<? super Object> asCommand(Object value) {
		return (HystrixCommand<? super Object>) value;
	}

	@SuppressWarnings("unchecked")
	private HystrixObservableCommand<? super Object> asObservableCommand(Object value) {
		return (HystrixObservableCommand<? super Object>) value;
	}

	@SuppressWarnings("unchecked")
	private Future<? super Object> asFuture(Object value) {
		return (Future<? super Object>) value;
	}

	@SuppressWarnings("unchecked")
	private Observable<? super Object> asObservable(Object value) {
		return (Observable<? super Object>) value;
	}
}