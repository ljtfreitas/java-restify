package com.restify.http.spring.client.request;

import java.lang.reflect.Type;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.contract.metadata.reflection.JavaType;

public class RestOperationsEndpointRequestExecutor implements EndpointRequestExecutor {

	private final RestOperations rest;
	private final RequestEntityConverter requestEntityConverter;
	private final EndpointResponseConverter responseEntityConverter;

	public RestOperationsEndpointRequestExecutor(RestOperations rest) {
		this(rest, new RequestEntityConverter(), new EndpointResponseConverter());
	}

	public RestOperationsEndpointRequestExecutor(RestOperations rest, RequestEntityConverter requestEntityConverter,
			EndpointResponseConverter responseEntityConverter) {
		this.rest = rest;
		this.requestEntityConverter = requestEntityConverter;
		this.responseEntityConverter = responseEntityConverter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EndpointResponse<T> execute(EndpointRequest endpointRequest) {
		RequestEntity<Object> request = requestEntityConverter.convert(endpointRequest);

		ResponseEntity<Object> response = rest.exchange(request, new JavaTypeReference(endpointRequest.responseType()));

		return (EndpointResponse<T>) responseEntityConverter.convert(response);
	}

	private class JavaTypeReference extends ParameterizedTypeReference<Object> {

		private final JavaType type;

		public JavaTypeReference(JavaType type) {
			this.type = type;
		}

		@Override
		public Type getType() {
			return type.unwrap();
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
			return "JavaTypeReference<" + type + ">";
		}
	}
}
