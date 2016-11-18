package com.restify.http.client.call;

import java.lang.reflect.ParameterizedType;

import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.request.EndpointRequestFactory;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;

public class EndpointCallFactory {

	private final EndpointRequestFactory endpointRequestFactory;
	private final EndpointRequestExecutor endpointRequestExecutor;

	public EndpointCallFactory(EndpointRequestFactory endpointRequestFactory,
			EndpointRequestExecutor endpointRequestExecutor) {
		this.endpointRequestFactory = endpointRequestFactory;
		this.endpointRequestExecutor = endpointRequestExecutor;
	}

	public <T> EndpointCall<T> createWith(EndpointMethod endpointMethod, Object[] args, JavaType returnType) {
		return doCreate(endpointMethod, args, returnType);
	}

	private <T> EndpointCall<T> doCreate(EndpointMethod endpointMethod, Object[] args, JavaType returnType) {
		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args, rawTypeOf(returnType));

		if (returnType.is(EndpointResponse.class)) {
			return endpointResponseCall(endpointRequest);

		} else {
			return new DefaultEndpointCall<>(endpointRequest, endpointRequestExecutor);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> EndpointCall<T> endpointResponseCall(EndpointRequest endpointRequest) {
		return (EndpointCall<T>) new EndpointResponseCall<>(endpointRequest, endpointRequestExecutor);
	}

	private JavaType rawTypeOf(JavaType returnType) {
		return returnType.is(EndpointResponse.class) ? rawParameterizedTypeOf(returnType) : returnType;
	}

	private JavaType rawParameterizedTypeOf(JavaType returnType) {
		return JavaType.of(returnType.parameterized() ? returnType.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class);
	}
}
