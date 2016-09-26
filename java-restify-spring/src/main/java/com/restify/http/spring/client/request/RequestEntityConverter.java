package com.restify.http.spring.client.request;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;

import com.restify.http.client.Headers;
import com.restify.http.client.request.EndpointRequest;

class RequestEntityConverter implements Converter<EndpointRequest, RequestEntity<Object>> {

	@Override
	public RequestEntity<Object> convert(EndpointRequest source) {
		Object body = source.body().orElse(null);

		HttpHeaders headers = headersOf(source.headers());

		HttpMethod method = HttpMethod.resolve(source.method());

		return new RequestEntity<>(body, headers, method, source.endpoint());
	}

	private HttpHeaders headersOf(Headers headers) {
		HttpHeaders httpHeaders = new HttpHeaders();

		headers.all().forEach(h -> httpHeaders.add(h.name(), h.value()));

		return httpHeaders;
	}

}
