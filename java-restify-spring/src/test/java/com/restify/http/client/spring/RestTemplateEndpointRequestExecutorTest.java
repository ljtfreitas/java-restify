package com.restify.http.client.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.lang.reflect.Type;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.restify.http.client.request.EndpointRequest;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

public class RestTemplateEndpointRequestExecutorTest {

	private RestOperationsEndpointRequestExecutor executor;

	private MockRestServiceServer server;

	@Before
	public void setup() {
		RestTemplate restTemplate = new RestTemplate();

		server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(requestTo("/my/api"))
			.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("{\"name\": \"Tiago de Freitas Lima\", \"age\": 31}", MediaType.APPLICATION_JSON));

		executor = new RestOperationsEndpointRequestExecutor(restTemplate);
	}

	@Test
	public void shouldExecuteRequestWithRestTemplate() {
		EndpointRequest endpointRequest = new EndpointRequest(URI.create("/my/api"), "GET", Model.class);

		Model response = (Model) executor.execute(endpointRequest);

		assertEquals("Tiago de Freitas Lima", response.name);
		assertEquals(31, response.age);

		server.verify();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnResponseWhenResponseObjectIsExpectedType() {
		Type expectedType = new SimpleParameterizedType(ResponseEntity.class, null, Model.class);

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("/my/api"), "GET", expectedType);

		ResponseEntity<Model> response = (ResponseEntity<Model>) executor.execute(endpointRequest);

		assertNotNull(response);

		Model responseBody = response.getBody();

		assertEquals("Tiago de Freitas Lima", responseBody.name);
		assertEquals(31, responseBody.age);

		server.verify();
	}

	public ResponseEntity<Model> xpto() {
		return null;
	}

	private static class Model {

		public String name;

		public int age;
	}
}
