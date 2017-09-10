package com.github.ljtfreitas.restify.http.spring.client.request;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.spring.client.request.EndpointResponseConverter;

public class EndpointResponseConverterTest {

	private ResponseEntity<Object> responseEntity;

	private EndpointResponseConverter converter;

	@Before
	public void setup() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.TEXT_PLAIN);
		httpHeaders.set("X-Header-Whatever", "whatever");

		responseEntity = new ResponseEntity<>("expected result", httpHeaders, HttpStatus.OK);

		converter = new EndpointResponseConverter();
	}

	@Test
	public void shouldConvertResponseEntityToRestifyEndpointResponse() {
		EndpointResponse<Object> endpointResponse = converter.convert(responseEntity);

		assertEquals(MediaType.TEXT_PLAIN_VALUE, endpointResponse.headers().get("Content-Type").get().value());
		assertEquals("whatever", endpointResponse.headers().get("X-Header-Whatever").get().value());

		assertEquals("expected result", endpointResponse.body());

		assertEquals(HttpStatus.OK.value(), endpointResponse.status().value());
	}
}
