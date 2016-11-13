package com.restify.http.client.call.exec;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class DefaultEndpointCallExecutableFactory<M> implements EndpointCallExecutableFactory<M, M> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return true;
	}

	@Override
	public EndpointCallExecutable<M, M> create(EndpointMethod endpointMethod) {
		return new DefaultEndpointMethodExecutable(endpointMethod.returnType());
	}

	public class DefaultEndpointMethodExecutable implements EndpointCallExecutable<M, M> {

		private JavaType type;

		public DefaultEndpointMethodExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public M execute(EndpointCall<M> call, Object[] args) {
			return call.execute();
		}
	}
}
