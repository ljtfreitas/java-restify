package com.restify.http.spring.client.response;

import org.springframework.http.ResponseEntity;

import com.restify.http.client.request.ExpectedType;

public class EndpointResponseEntity {

	private final ResponseEntity<Object> response;
	private final ExpectedType expectedType;

	public EndpointResponseEntity(ResponseEntity<Object> response, ExpectedType expectedType) {
		this.response = response;
		this.expectedType = expectedType;
	}

	public ResponseEntity<Object> entity() {
		return response;
	}

	public ExpectedType expectedType() {
		return expectedType;
	}
}
