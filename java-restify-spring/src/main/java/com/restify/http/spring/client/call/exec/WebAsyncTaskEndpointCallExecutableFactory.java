package com.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.context.request.async.WebAsyncTask;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class WebAsyncTaskEndpointCallExecutableFactory<T, O> implements EndpointCallExecutableDecoratorFactory<WebAsyncTask<T>, T, O> {

	private final Long timeout;
	private final AsyncTaskExecutor asyncTaskExecutor;

	public WebAsyncTaskEndpointCallExecutableFactory() {
		this(new SimpleAsyncTaskExecutor());
	}

	public WebAsyncTaskEndpointCallExecutableFactory(AsyncTaskExecutor asyncTaskExecutor) {
		this(null, asyncTaskExecutor);
	}

	public WebAsyncTaskEndpointCallExecutableFactory(Long timeout) {
		this(timeout, new SimpleAsyncTaskExecutor());
	}

	public WebAsyncTaskEndpointCallExecutableFactory(Long timeout, AsyncTaskExecutor asyncTaskExecutor) {
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
	public EndpointCallExecutable<WebAsyncTask<T>, O> create(EndpointMethod endpointMethod, EndpointCallExecutable<T, O> executable) {
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
