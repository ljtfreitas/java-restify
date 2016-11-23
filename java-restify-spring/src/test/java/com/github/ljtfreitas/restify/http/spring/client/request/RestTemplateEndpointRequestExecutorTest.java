package com.github.ljtfreitas.restify.http.spring.client.request;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.spring.client.request.RestOperationsEndpointRequestExecutor;

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

		EndpointResponse<Model> response = executor.execute(endpointRequest);

		Model model = response.body();

		assertEquals("Tiago de Freitas Lima", model.name);
		assertEquals(31, model.age);

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
