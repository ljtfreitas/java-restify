package com.restify.http.spring.client.call.exec;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.restify.http.client.Headers;
import com.restify.http.client.response.EndpointResponse;

class ResponseEntityConverter<T> implements Converter<EndpointResponse<T>, ResponseEntity<T>> {

	@Override
	public ResponseEntity<T> convert(EndpointResponse<T> source) {
		return new ResponseEntity<T>(source.body(), headersOf(source.headers()), HttpStatus.valueOf(source.code().value()));
	}

	private HttpHeaders headersOf(Headers headers) {
		HttpHeaders httpHeaders = new HttpHeaders();
		headers.all().forEach(h -> httpHeaders.add(h.name(), h.value()));
		return httpHeaders;
	}

}
