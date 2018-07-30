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
package com.github.ljtfreitas.restify.http.client.call.exec.rxjava2;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class RxJava2CompletableEndpointCallExecutableFactory implements AsyncEndpointCallExecutableFactory<Completable, Void> {

	public final Scheduler scheduler;

	public RxJava2CompletableEndpointCallExecutableFactory() {
		this.scheduler = Schedulers.io();
	}

	public RxJava2CompletableEndpointCallExecutableFactory(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Completable.class);
	}

	@Override
	public AsyncEndpointCallExecutable<Completable, Void> createAsync(EndpointMethod endpointMethod) {
		return new RxJava2CompletableEndpointCallExecutable(endpointMethod.returnType());
	}

	private class RxJava2CompletableEndpointCallExecutable implements AsyncEndpointCallExecutable<Completable, Void> {

		private final JavaType javaType;

		public RxJava2CompletableEndpointCallExecutable(JavaType javaType) {
			this.javaType = javaType;
		}

		@Override
		public JavaType returnType() {
			return javaType;
		}

		@Override
		public Completable execute(EndpointCall<Void> call, Object[] args) {
			return Completable.fromRunnable(call::execute)
					.subscribeOn(scheduler);
		}

		@Override
		public Completable executeAsync(AsyncEndpointCall<Void> call, Object[] args) {
			return Completable.fromFuture(call.executeAsync().toCompletableFuture())
				.subscribeOn(scheduler);
		}
	}
}
