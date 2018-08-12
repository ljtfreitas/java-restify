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
package com.github.ljtfreitas.restify.http.spring.client.call.exec.async;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.context.request.async.WebAsyncTask;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class WebAsyncTaskEndpointCallExecutableAdapter<T, O> implements EndpointCallExecutableAdapter<WebAsyncTask<T>, T, O> {

	private final Long timeout;
	private final AsyncTaskExecutor asyncTaskExecutor;

	public WebAsyncTaskEndpointCallExecutableAdapter() {
		this(new SimpleAsyncTaskExecutor());
	}

	public WebAsyncTaskEndpointCallExecutableAdapter(AsyncTaskExecutor asyncTaskExecutor) {
		this(null, asyncTaskExecutor);
	}

	public WebAsyncTaskEndpointCallExecutableAdapter(Long timeout) {
		this(timeout, new SimpleAsyncTaskExecutor());
	}

	public WebAsyncTaskEndpointCallExecutableAdapter(Long timeout, AsyncTaskExecutor asyncTaskExecutor) {
		this.timeout = timeout;
		this.asyncTaskExecutor = asyncTaskExecutor;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(WebAsyncTask.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(unwrap(endpointMethod.returnType()));
	}

	private Type unwrap(JavaType declaredReturnType) {
		return declaredReturnType.parameterized() ?
				declaredReturnType.as(ParameterizedType.class).getActualTypeArguments()[0] :
					Object.class;
	}

	@Override
	public EndpointCallExecutable<WebAsyncTask<T>, O> adapt(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
		return new WebAsyncTaskEndpointCallExecutable(executable);
	}

	private class WebAsyncTaskEndpointCallExecutable implements EndpointCallExecutable<WebAsyncTask<T>, O> {

		private final EndpointCallExecutable<T, O> delegate;

		public WebAsyncTaskEndpointCallExecutable(EndpointCallExecutable<T, O> executable) {
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public WebAsyncTask<T> execute(EndpointCall<O> call, Object[] args) {
			return new WebAsyncTask<T>(timeout, asyncTaskExecutor, () -> delegate.execute(call, args));
		}
	}
}