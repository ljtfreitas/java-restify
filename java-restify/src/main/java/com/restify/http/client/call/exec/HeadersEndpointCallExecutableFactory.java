package com.restify.http.client.call.exec;

import com.restify.http.client.Headers;
import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

public class HeadersEndpointCallExecutableFactory implements EndpointCallExecutableDecoratorFactory<Headers, EndpointResponse<Void>, Void> {

	private static final JavaType DEFAULT_RETURN_TYPE = JavaType.of(new SimpleParameterizedType(EndpointResponse.class, null, Void.class));

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Headers.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return DEFAULT_RETURN_TYPE;
	}

	@Override
	public EndpointCallExecutable<Headers, Void> create(EndpointMethod endpointMethod, EndpointCallExecutable<EndpointResponse<Void>, Void> executable) {
		return new HeadersEndpointCallExecutable(executable);
	}

	public class HeadersEndpointCallExecutable implements EndpointCallExecutable<Headers, Void> {

		private final EndpointCallExecutable<EndpointResponse<Void>, Void> delegate;

		public HeadersEndpointCallExecutable(EndpointCallExecutable<EndpointResponse<Void>, Void> executable) {
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Headers execute(EndpointCall<Void> call, Object[] args) {
			return delegate.execute(call, args).headers();
		}
	}
}
