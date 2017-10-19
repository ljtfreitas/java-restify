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
package com.github.ljtfreitas.restify.http.netflix.client.call.exec;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.netflix.hystrix.HystrixCommand;

class EndpointCallExecutableHystrixCommand<T, R> extends HystrixCommand<T> {

	private final EndpointCallExecutable<T, R> delegate;
	private final EndpointCall<R> call;
	private final Object[] args;
	private final HystrixCommandFallback fallback;

	EndpointCallExecutableHystrixCommand(HystrixCommand.Setter hystrixMetadata, EndpointCallExecutable<T, R> delegate,
			EndpointCall<R> call, Object[] args, HystrixCommandFallback fallback) {
		super(hystrixMetadata);
		this.delegate = delegate;
		this.call = call;
		this.args = args;
		this.fallback = fallback;
	}

	@Override
	protected T run() throws Exception {
		return delegate.execute(call, args);
	}

	@Override
	protected T getFallback() {
		return fallback == null ? super.getFallback() : doFallback();
	}

	private T doFallback() {
		return fallback.run();
	}
}