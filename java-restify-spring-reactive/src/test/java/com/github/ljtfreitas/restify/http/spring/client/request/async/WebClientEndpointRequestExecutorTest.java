package com.github.ljtfreitas.restify.http.spring.client.request.async;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseInternalServerErrorException;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class WebClientEndpointRequestExecutorTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080, 7084);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MockServerClient mockServerClient;

	private WebClientEndpointRequestExecutor executor;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		executor = new WebClientEndpointRequestExecutor();
	}

	@Test
	public void shouldSendGetRequest() throws Exception {
		String responseBody = "{\"name\": \"Tiago de Freitas Lima\",\"age\":31}";

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withHeader("X-Custom-Header", "whatever")
					.withBody(json(responseBody)));

		CompletableFuture<EndpointResponse<Person>> future = executor
				.executeAsync(new EndpointRequest(URI.create("http://localhost:7080/json"), "GET", JavaType.of(Person.class)));

		EndpointResponse<Person> response = future.get();

		assertEquals(StatusCode.ok(), response.status());
		assertEquals("whatever", response.headers().get("X-Custom-Header").map(Header::value).orElse(null));

		Person person = response.body();

		assertEquals("Tiago de Freitas Lima", person.name);
		assertEquals(31, person.age);
	}

	@Test
	public void shouldSendPostRequest() throws Exception {
		String requestBody = "{\"name\":\"Tiago de Freitas Lima\",\"age\":31}";
		String responseBody = "OK";

		HttpRequest httpRequest = request()
			.withMethod("POST")
			.withPath("/json")
			.withHeader("Content-Type", "application/json")
			.withBody(json(requestBody));

		mockServerClient
			.when(httpRequest)
			.respond(response()
				.withStatusCode(201)
				.withHeader("Content-Type", "text/plain")
				.withBody(exact(responseBody)));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/json"), "POST",
				new Headers(Header.contentType("application/json")), new Person("Tiago de Freitas Lima", 31), JavaType.of(String.class));

		CompletableFuture<EndpointResponse<String>> future = executor
				.executeAsync(endpointRequest);

		EndpointResponse<String> response = future.get();

		assertEquals("OK", response.body());
		assertEquals(StatusCode.created(), response.status());
	}

	@Test
	public void shouldSendPutRequest() throws Exception {
		String requestBody = "{\"name\":\"Tiago de Freitas Lima\",\"age\":31}";
		String responseBody = "OK";

		HttpRequest httpRequest = request()
			.withMethod("PUT")
			.withPath("/json")
			.withHeader("Content-Type", "application/json")
			.withBody(json(requestBody));

		mockServerClient
			.when(httpRequest)
			.respond(response()
				.withStatusCode(200)
				.withHeader("Content-Type", "text/plain")
				.withBody(exact(responseBody)));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/json"), "PUT",
				new Headers(Header.contentType("application/json")), new Person("Tiago de Freitas Lima", 31), JavaType.of(String.class));

		CompletableFuture<EndpointResponse<String>> future = executor
				.executeAsync(endpointRequest);

		EndpointResponse<String> response = future.get();

		assertEquals("OK", response.body());
		assertEquals(StatusCode.ok(), response.status());
	}

	@Test
	public void shouldSendDeleteRequest() throws Exception {
		HttpRequest httpRequest = request()
			.withMethod("DELETE")
			.withPath("/person/abc123");

		mockServerClient
			.when(httpRequest)
			.respond(response()
				.withStatusCode(200)
				.withHeader("X-Custom-Header", "deleted"));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/person/abc123"), "DELETE");

		CompletableFuture<EndpointResponse<Void>> future = executor
				.executeAsync(endpointRequest);

		EndpointResponse<Void> response = future.get();

		assertEquals(StatusCode.ok(), response.status());
		assertEquals("deleted", response.headers().get("X-Custom-Header").map(Header::value).orElse(null));
		assertNull(response.body());
	}

	@Test
	public void shouldWrapIOExceptionWhenOcurred() throws Exception {
		CompletableFuture<EndpointResponse<Void>> future = executor
				.executeAsync(new EndpointRequest(URI.create("http://localhost:7777/error"), "GET"));

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpClientException.class));

		future.get();
	}

	@Test
	public void shouldWrapResponseExceptionWhenOcurred() throws Exception {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/error"))
			.respond(response()
					.withStatusCode(500));

		CompletableFuture<EndpointResponse<Void>> future = executor
				.executeAsync(new EndpointRequest(URI.create("http://localhost:7080/error"), "GET"));

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(EndpointResponseInternalServerErrorException.class));

		future.get();
	}

	private static class Person {

		@JsonProperty
		private String name;

		@JsonProperty
		private int age;

		Person(@JsonProperty("name") String name, @JsonProperty("age") int age) {
			super();
			this.name = name;
			this.age = age;
		}
	}
}
