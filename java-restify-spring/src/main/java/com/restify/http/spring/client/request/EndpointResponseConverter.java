package com.restify.http.spring.client.request;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.restify.http.client.Headers;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.client.response.EndpointResponseCode;

class EndpointResponseConverter implements Converter<ResponseEntity<Object>, EndpointResponse<Object>> {

	@Override
	public EndpointResponse<Object> convert(ResponseEntity<Object> source) {
		EndpointResponseCode status = EndpointResponseCode.of(source.getStatusCodeValue());
		Headers headers = headersOf(source.getHeaders());
		return new EndpointResponse<>(status, headers, source.getBody());
	}

	private Headers headersOf(HttpHeaders httpHeaders) {
		Headers headers = new Headers();
		httpHeaders.forEach((k, v) -> headers.put(k, v));
		return headers;
	}
}
