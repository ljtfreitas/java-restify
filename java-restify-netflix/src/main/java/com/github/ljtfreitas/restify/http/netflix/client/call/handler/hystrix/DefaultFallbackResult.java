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

class DefaultFallbackResult implements FallbackResult<Object> {

	private final Object value;

	DefaultFallbackResult(Object value) {
		this.value = value;
	}

	@Override
	public Object get() throws Exception {
		if (value instanceof HystrixCommand) {
			return asCommand().execute();

		} else if (value instanceof HystrixObservableCommand) {
			return asObservableCommand().toObservable().toBlocking().singleOrDefault(null);

		} else if (value instanceof Future) {
			return asFuture().get();

		} else if (value instanceof Observable) {
			return asObservable().toBlocking().singleOrDefault(null);

		} else return value;
	}


	@SuppressWarnings("unchecked")
	private HystrixCommand<? super Object> asCommand() {
		return (HystrixCommand<? super Object>) value;
	}

	@SuppressWarnings("unchecked")
	private HystrixObservableCommand<? super Object> asObservableCommand() {
		return (HystrixObservableCommand<? super Object>) value;
	}

	@SuppressWarnings("unchecked")
	private Future<? super Object> asFuture() {
		return (Future<? super Object>) value;
	}

	@SuppressWarnings("unchecked")
	private Observable<? super Object> asObservable() {
		return (Observable<? super Object>) value;
	}
}