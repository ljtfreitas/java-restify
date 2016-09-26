package com.restify.http.spring.client.response;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.restify.http.client.Headers;
import com.restify.http.client.request.ExpectedType;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.client.response.EndpointResponseCode;

public class ResponseEntityConverter implements Converter<EndpointResponseEntity, Object> {

	@Override
	public Object convert(EndpointResponseEntity source) {
		ExpectedType expectedType = source.expectedType();
		ResponseEntity<Object> responseEntity = source.entity();

		if (expectedType.voidType()) {
			return null;

		} else if (expectedType.is(Headers.class)) {
			return headersOf(responseEntity.getHeaders());

		} else if (expectedType.is(HttpHeaders.class)) {
			return responseEntity.getHeaders();

		} else if (expectedType.is(EndpointResponse.class)) {
			return responseOf(responseEntity);

		} else if (expectedType.is(ResponseEntity.class)) {
			return responseEntity;

		} else {
			return responseEntity.getBody();
		}

	}

	private EndpointResponse<? extends Object> responseOf(ResponseEntity<Object> responseEntity) {
		return new EndpointResponse<>(EndpointResponseCode.of(responseEntity.getStatusCodeValue()),
				headersOf(responseEntity.getHeaders()), responseEntity.getBody());
	}

	private Headers headersOf(HttpHeaders httpHeaders) {
		Headers headers = new Headers();

		httpHeaders.forEach((k, v) -> headers.put(k, v));

		return headers;
	}

}
