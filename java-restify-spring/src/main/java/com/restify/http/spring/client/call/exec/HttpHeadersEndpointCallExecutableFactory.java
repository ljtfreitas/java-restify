package com.restify.http.spring.client.call.exec;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

public class HttpHeadersEndpointCallExecutableFactory implements EndpointCallExecutableFactory<HttpHeaders, EndpointResponse<Object>> {

	private final Converter<EndpointResponse<Object>, ResponseEntity<Object>> endpointResponseConverter;

	public HttpHeadersEndpointCallExecutableFactory() {
		this(new ResponseEntityConverter<>());
	}

	public HttpHeadersEndpointCallExecutableFactory(Converter<EndpointResponse<Object>, ResponseEntity<Object>> endpointResponseConverter) {
		this.endpointResponseConverter = endpointResponseConverter;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(HttpHeaders.class);
	}

	@Override
	public EndpointCallExecutable<HttpHeaders, EndpointResponse<Object>> create(EndpointMethod endpointMethod) {
		return new HttpHeadersEndpointCallExecutable(JavaType.of(new SimpleParameterizedType(EndpointResponse.class, null, Void.class)));
	}

	private class HttpHeadersEndpointCallExecutable implements EndpointCallExecutable<HttpHeaders, EndpointResponse<Object>> {

		private final JavaType returnType;

		private HttpHeadersEndpointCallExecutable(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public HttpHeaders execute(EndpointCall<EndpointResponse<Object>> call, Object[] args) {
			EndpointResponse<Object> response = call.execute();
			return endpointResponseConverter.convert(response).getHeaders();
		}
	}
}
