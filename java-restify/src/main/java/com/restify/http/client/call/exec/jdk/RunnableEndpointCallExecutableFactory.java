package com.restify.http.client.call.exec.jdk;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class RunnableEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<Runnable, T> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Runnable.class);
	}

	@Override
	public EndpointCallExecutable<Runnable, T> create(EndpointMethod endpointMethod) {
		return new RunnableEndpointMethodExecutable(JavaType.of(void.class));
	}

	private class RunnableEndpointMethodExecutable implements EndpointCallExecutable<Runnable, T> {

		private final JavaType type;

		private RunnableEndpointMethodExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public Runnable execute(EndpointCall<T> call) {
			return () -> call.execute();
		}
	}
}
