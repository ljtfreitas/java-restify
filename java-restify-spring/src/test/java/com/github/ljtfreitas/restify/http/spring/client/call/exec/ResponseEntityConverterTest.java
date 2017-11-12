package com.github.ljtfreitas.restify.http.spring.client.call.exec;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

public class ResponseEntityConverterTest {

	private ResponseEntityConverter<String> converter = new ResponseEntityConverter<String>();

	private EndpointResponse<String> endpointResponse;

	@Before
	public void setup() {
		Headers headers = new Headers()
				.add(Header.contentType(MediaType.TEXT_PLAIN_VALUE))
				.add(Header.of("X-Header-Whatever", "whatever"));

		endpointResponse = new EndpointResponse<>(StatusCode.ok(), headers, "expected result");
	}

	@Test
	public void shouldConvertRestifyEndpointResponseToResponseEntity() {
		ResponseEntity<String> responseEntity = converter.convert(endpointResponse);

		assertEquals(MediaType.TEXT_PLAIN, responseEntity.getHeaders().getContentType());
		assertEquals(Arrays.asList("whatever"), responseEntity.getHeaders().get("X-Header-Whatever"));

		assertEquals("expected result", responseEntity.getBody());

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}
}
