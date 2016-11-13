package com.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.ResponseEntity;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

public class ResponseEntityEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<ResponseEntity<T>, EndpointResponse<T>> {

	private final Converter<EndpointResponse<T>, ResponseEntity<T>> endpointResponseConverter;

	public ResponseEntityEndpointCallExecutableFactory() {
		this(new ResponseEntityConverter<>());
	}

	public ResponseEntityEndpointCallExecutableFactory(Converter<EndpointResponse<T>, ResponseEntity<T>> endpointResponseConverter) {
		this.endpointResponseConverter = endpointResponseConverter;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(ResponseEntity.class);
	}

	@Override
	public EndpointCallExecutable<ResponseEntity<T>, EndpointResponse<T>> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new ResponseEntityEndpointCallExecutable(JavaType.of(new SimpleParameterizedType(EndpointResponse.class, null, responseType)));
	}

	private class ResponseEntityEndpointCallExecutable implements EndpointCallExecutable<ResponseEntity<T>, EndpointResponse<T>> {

		private final JavaType returnType;

		private ResponseEntityEndpointCallExecutable(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public ResponseEntity<T> execute(EndpointCall<EndpointResponse<T>> call, Object[] args) {
			EndpointResponse<T> response = call.execute();
			return endpointResponseConverter.convert(response);
		}
	}
}
