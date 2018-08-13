package com.github.ljtfreitas.restify.http.client.call.handler;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

class DefaultEndpointCallHandlerFactory<M> implements EndpointCallHandlerFactory<M, M> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return true;
	}

	@Override
	public EndpointCallHandler<M, M> create(EndpointMethod endpointMethod) {
		return new DefaultEndpointMethodExecutable(endpointMethod.returnType());
	}

	public class DefaultEndpointMethodExecutable implements EndpointCallHandler<M, M> {

		private JavaType type;

		public DefaultEndpointMethodExecutable(JavaType type) {
			this.type = type;
		}

		@Override
		public JavaType returnType() {
			return type;
		}

		@Override
		public M handle(EndpointCall<M> call, Object[] args) {
			return call.execute();
		}
	}
}
