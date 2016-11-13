package com.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.web.context.request.async.DeferredResult;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class DeferredResultEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<DeferredResult<T>, T> {

	private final Long timeout;
	private final Executor executor;

	public DeferredResultEndpointCallExecutableFactory() {
		this(Executors.newSingleThreadExecutor());
	}

	public DeferredResultEndpointCallExecutableFactory(Executor executor) {
		this(null, executor);
	}

	public DeferredResultEndpointCallExecutableFactory(Long timeout) {
		this(timeout, Executors.newSingleThreadExecutor());
	}

	public DeferredResultEndpointCallExecutableFactory(Long timeout, Executor executor) {
		this.timeout = timeout;
		this.executor = executor;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(DeferredResult.class);
	}

	@Override
	public EndpointCallExecutable<DeferredResult<T>, T> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new DeferredResultEndpointCallExecutable(JavaType.of(responseType));
	}

	private class DeferredResultEndpointCallExecutable implements EndpointCallExecutable<DeferredResult<T>, T> {

		private final JavaType returnType;

		private DeferredResultEndpointCallExecutable(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public DeferredResult<T> execute(EndpointCall<T> call, Object[] args) {
			DeferredResult<T> deferredResult = new DeferredResult<>(timeout);

			CompletableFuture.supplyAsync(() -> call.execute(), executor)
				.whenComplete((r, ex) -> {
					if (r != null) {
						deferredResult.setResult(r);
					} else if (ex != null) {
						deferredResult.setErrorResult(ex);
					}
				});

			return deferredResult;
		}
	}
}
