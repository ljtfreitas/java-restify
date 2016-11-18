package com.restify.http.spring.client.call.exec;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableDecoratorFactory;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

public class HttpHeadersEndpointCallExecutableFactory implements EndpointCallExecutableDecoratorFactory<HttpHeaders, ResponseEntity<Void>, Void> {

	private static final JavaType DEFAULT_RETURN_TYPE = JavaType.of(new SimpleParameterizedType(ResponseEntity.class, null, Void.class));

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(HttpHeaders.class);
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return DEFAULT_RETURN_TYPE;
	}

	@Override
	public EndpointCallExecutable<HttpHeaders, Void> create(EndpointMethod endpointMethod, EndpointCallExecutable<ResponseEntity<Void>, Void> executable) {
		return new HttpHeadersEndpointCallExecutable(executable);
	}

	private class HttpHeadersEndpointCallExecutable implements EndpointCallExecutable<HttpHeaders, Void> {

		private final EndpointCallExecutable<ResponseEntity<Void>, Void> delegate;

		public HttpHeadersEndpointCallExecutable(EndpointCallExecutable<ResponseEntity<Void>, Void> executable) {
			this.delegate = executable;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public HttpHeaders execute(EndpointCall<Void> call, Object[] args) {
			return delegate.execute(call, args).getHeaders();
		}
	}
}
