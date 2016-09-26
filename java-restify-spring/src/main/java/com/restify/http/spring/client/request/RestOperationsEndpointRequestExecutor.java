package com.restify.http.spring.client.request;

import java.lang.reflect.Type;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.restify.http.client.request.ExpectedType;
import com.restify.http.spring.client.response.EndpointResponseEntity;
import com.restify.http.spring.client.response.ResponseEntityConverter;
import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.EndpointRequestExecutor;

public class RestOperationsEndpointRequestExecutor implements EndpointRequestExecutor {

	private final RestOperations rest;
	private final RequestEntityConverter requestEntityConverter;
	private final ResponseEntityConverter responseEntityConverter;

	public RestOperationsEndpointRequestExecutor(RestOperations rest) {
		this(rest, new RequestEntityConverter(), new ResponseEntityConverter());
	}

	public RestOperationsEndpointRequestExecutor(RestOperations rest, RequestEntityConverter requestEntityConverter,
			ResponseEntityConverter responseEntityConverter) {
		this.rest = rest;
		this.requestEntityConverter = requestEntityConverter;
		this.responseEntityConverter = responseEntityConverter;
	}

	@Override
	public Object execute(EndpointRequest endpointRequest) {
		RequestEntity<Object> request = requestEntityConverter.convert(endpointRequest);

		ExpectedType requestExpectedType = endpointRequest.expectedType().dispose(ResponseEntity.class);

		ResponseEntity<Object> response = rest.exchange(request, new ExpectedTypeReference(requestExpectedType.type()));

		return responseEntityConverter.convert(new EndpointResponseEntity(response, requestExpectedType));
	}

	private class ExpectedTypeReference extends ParameterizedTypeReference<Object> {

		private final Type type;

		public ExpectedTypeReference(Type type) {
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public boolean equals(Object obj) {
			return (this == obj || (obj instanceof ParameterizedTypeReference
					&& type.equals(((ParameterizedTypeReference<?>) obj).getType())));
		}

		@Override
		public int hashCode() {
			return type.hashCode();
		}

		@Override
		public String toString() {
			return "ExpectedTypeReference<" + type + ">";
		}
	}
}
