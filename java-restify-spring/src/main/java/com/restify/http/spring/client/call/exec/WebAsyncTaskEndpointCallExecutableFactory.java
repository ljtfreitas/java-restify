package com.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.context.request.async.WebAsyncTask;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class WebAsyncTaskEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<WebAsyncTask<T>, T> {

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
	public EndpointCallExecutable<WebAsyncTask<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new WebAsyncTaskEndpointCallExecutable(JavaType.of(responseType));
	}

	private class WebAsyncTaskEndpointCallExecutable implements EndpointCallExecutable<WebAsyncTask<T>, T> {

		private final JavaType returnType;

		private WebAsyncTaskEndpointCallExecutable(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public WebAsyncTask<T> execute(EndpointCall<T> call, Object[] args) {
			return new WebAsyncTask<T>(timeout, asyncTaskExecutor, () -> call.execute());
		}
	}
}
