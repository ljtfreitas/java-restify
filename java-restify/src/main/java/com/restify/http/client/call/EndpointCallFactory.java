package com.restify.http.client.call;

import java.lang.reflect.ParameterizedType;

import com.restify.http.client.Headers;
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
		if (returnType.is(EndpointCall.class)) {
			return endpointCallDecorator(endpointMethod, args, rawTypeOf(returnType));

		} else {
			return doCreate(endpointMethod, args, returnType);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> EndpointCall<T> endpointCallDecorator(EndpointMethod endpointMethod, Object[] args, JavaType returnType) {
		return (EndpointCall<T>) new EndpointCallDecorator<T>(doCreate(endpointMethod, args, returnType));
	}

	private <T> EndpointCall<T> doCreate(EndpointMethod endpointMethod, Object[] args, JavaType returnType) {
		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args, rawTypeOf(returnType));

		if (returnType.is(EndpointResponse.class)) {
			return endpointRequestCall(endpointRequest);

		} else if (returnType.is(Headers.class)) {
			return endpointHeadersCall(endpointRequest);

		} else {
			return new DefaultEndpointCall<>(endpointRequest, endpointRequestExecutor);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> EndpointCall<T> endpointHeadersCall(EndpointRequest endpointRequest) {
		return (EndpointCall<T>) new EndpointHeadersCall(endpointRequest, endpointRequestExecutor);
	}

	@SuppressWarnings("unchecked")
	private <T> EndpointCall<T> endpointRequestCall(EndpointRequest endpointRequest) {
		return (EndpointCall<T>) new EndpointResponseCall<>(endpointRequest, endpointRequestExecutor);
	}

	private JavaType rawTypeOf(JavaType returnType) {
		return returnType.is(EndpointResponse.class) || returnType.is(EndpointCall.class) ? rawParameterizedTypeOf(returnType) : returnType;
	}

	private JavaType rawParameterizedTypeOf(JavaType returnType) {
		return JavaType.of(returnType.parameterized() ? returnType.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class);
	}
}
