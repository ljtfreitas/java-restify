package com.restify.http.client.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.restify.http.client.Headers;
import com.restify.http.client.request.ExpectedType;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

public class ResponseEntityConverterTest {

	private ResponseEntityConverter converter = new ResponseEntityConverter();

	private ResponseEntity<Object> responseEntity;

	@Before
	public void setup() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.TEXT_PLAIN);
		httpHeaders.set("X-Header-Whatever", "whatever");

		responseEntity = new ResponseEntity<>("expected result", httpHeaders, HttpStatus.OK);
	}

	@Test
	public void shouldReturnNullWhenExpectedTypeIsVoid() {
		EndpointResponseEntity endpointResponseEntity = new EndpointResponseEntity(responseEntity,
				ExpectedType.of(Void.class));

		Object result = converter.convert(endpointResponseEntity);

		assertNull(result);
	}

	@Test
	public void shouldReturnRestifyHeadersObjectWhenItIsExpectedType() {
		EndpointResponseEntity endpointResponseEntity = new EndpointResponseEntity(responseEntity,
				ExpectedType.of(Headers.class));

		Headers result = (Headers) converter.convert(endpointResponseEntity);

		assertNotNull(result);

		assertEquals("text/plain", result.get("Content-Type").get().value());
		assertEquals("whatever", result.get("X-Header-Whatever").get().value());
	}

	@Test
	public void shouldReturnEntityHttpHeadersWhenItIsExpectedType() {
		EndpointResponseEntity endpointResponseEntity = new EndpointResponseEntity(responseEntity,
				ExpectedType.of(HttpHeaders.class));

		HttpHeaders result = (HttpHeaders) converter.convert(endpointResponseEntity);

		assertNotNull(result);

		assertSame(responseEntity.getHeaders(), result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnRestifyResponseObjectWithEntityBodyWhenItIsExpectedType() {
		EndpointResponseEntity endpointResponseEntity = new EndpointResponseEntity(responseEntity,
				ExpectedType.of(new SimpleParameterizedType(EndpointResponse.class, null, String.class)));

		EndpointResponse<String> result = (EndpointResponse<String>) converter.convert(endpointResponseEntity);

		assertNotNull(result);
		assertEquals(responseEntity.getBody(), result.body());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnTheSameResponseEntityWhenItIsExpectedType() {
		EndpointResponseEntity endpointResponseEntity = new EndpointResponseEntity(responseEntity,
				ExpectedType.of(new SimpleParameterizedType(ResponseEntity.class, null, String.class)));

		ResponseEntity<String> result = (ResponseEntity<String>) converter.convert(endpointResponseEntity);

		assertNotNull(result);
		assertSame(responseEntity, result);
	}

	@Test
	public void shouldReturnBodyOfResponseEntityWhenSimpleResponseType() {
		EndpointResponseEntity endpointResponseEntity = new EndpointResponseEntity(responseEntity,
				ExpectedType.of(String.class));

		Object result = converter.convert(endpointResponseEntity);

		assertEquals(responseEntity.getBody(), result);
	}
}
