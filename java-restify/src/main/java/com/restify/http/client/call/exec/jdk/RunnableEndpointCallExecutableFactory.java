package com.restify.http.client.call.exec.jdk;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class RunnableEndpointCallExecutableFactory implements EndpointCallExecutableFactory<Runnable, Void> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Runnable.class);
	}

	@Override
	public EndpointCallExecutable<Runnable, Void> create(EndpointMethod endpointMethod) {
		return new RunnableEndpointMethodExecutable(JavaType.of(Void.class));
	}

	private class RunnableEndpointMethodExecutable implements EndpointCallExecutable<Runnable, Void> {

		private final JavaType type;

		private RunnableEndpointMethodExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public Runnable execute(EndpointCall<Void> call, Object[] args) {
			return () -> call.execute();
		}
	}
}
