package com.github.ljtfreitas.restify.http.spring.client.request;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Arrays;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.spring.client.request.RequestEntityConverter;

public class RequestEntityConverterTest {

	private RequestEntityConverter converter = new RequestEntityConverter();

	@Test
	public void shouldConvertRestifyEndpointRequestToRequestEntity() {
		URI endpoint = URI.create("http://my.api.com/path");

		Headers headers = new Headers()
				.add(Header.contentType("application/json"))
				.add(Header.accept("application/json"));

		MyRequestModel body = new MyRequestModel();

		EndpointRequest source = new EndpointRequest(endpoint, "GET", headers, body, MyResponseModel.class);

		RequestEntity<Object> entity = converter.convert(source);

		assertEquals(endpoint, entity.getUrl());
		assertEquals(HttpMethod.GET, entity.getMethod());

		HttpHeaders entityHeaders = entity.getHeaders();
		assertEquals(MediaType.APPLICATION_JSON, entityHeaders.getContentType());
		assertEquals(Arrays.asList(MediaType.APPLICATION_JSON), entityHeaders.getAccept());

		assertEquals(MyResponseModel.class, MyResponseModel.class);
	}

	private class MyRequestModel {
	}

	private class MyResponseModel {
	}
}
