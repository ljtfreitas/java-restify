package com.github.ljtfreitas.restify.http.spring.client.call.exec;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseInternalServerErrorException;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;

public class ResponseEntityConverterTest {

	private ResponseEntityConverter<String> converter = new ResponseEntityConverter<String>();

	private EndpointResponse<String> endpointResponse;

	@Before
	public void setup() {
		Headers headers = new Headers();
		headers.add(Header.contentType(MediaType.TEXT_PLAIN_VALUE));
		headers.add(Header.of("X-Header-Whatever", "whatever"));

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

	@Test(expected = RestifyEndpointResponseInternalServerErrorException.class)
	public void shouldThrowExceptionWhenEndpointResponseIsError() {
		RestifyEndpointResponseInternalServerErrorException exception = new RestifyEndpointResponseInternalServerErrorException("Internal Server Error",
				new Headers(), "oops...");

		endpointResponse = EndpointResponse.error(exception);

		converter.convert(endpointResponse);
	}
}
