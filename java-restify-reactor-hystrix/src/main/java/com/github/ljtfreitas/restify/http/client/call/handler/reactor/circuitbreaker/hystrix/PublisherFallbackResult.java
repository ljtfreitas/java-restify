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
package com.github.ljtfreitas.restify.http.client.call.handler.reactor.circuitbreaker.hystrix;

import org.reactivestreams.Publisher;

import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.FallbackResult;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;

class PublisherFallbackResult<T> implements FallbackResult<Publisher<T>> {

	private final FallbackResult<T> delegate;

	PublisherFallbackResult(FallbackResult<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Publisher<T> get() {
		T value = delegate.get();

		if (value instanceof Publisher) {
			return asPublisher(value);

		} else if (value instanceof Iterable) {
			return Flux.fromIterable(asIterable(value));

		} else if (value instanceof Observable) {
			return RxReactiveStreams.toPublisher(asObservable(value));

		} else if (value == null) {
			return Mono.empty();

		} else return Mono.just(value);
	}

	@SuppressWarnings("unchecked")
	private Observable<T> asObservable(T value) {
		return (Observable<T>) value;
	}

	@SuppressWarnings("unchecked")
	private Iterable<T> asIterable(T value) {
		return (Iterable<T>) value;
	}

	@SuppressWarnings("unchecked")
	private Publisher<T> asPublisher(T value) {
		return (Publisher<T>) value;
	}

}
