package com.restify.http.client.spring;

import org.springframework.http.ResponseEntity;

import com.restify.http.client.request.ExpectedType;

class EndpointResponseEntity {

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
