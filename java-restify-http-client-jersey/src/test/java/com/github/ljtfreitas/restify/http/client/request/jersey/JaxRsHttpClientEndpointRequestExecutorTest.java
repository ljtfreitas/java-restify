package com.github.ljtfreitas.restify.http.client.request.jersey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.net.URI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.jersey.JaxRsHttpClientEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseInternalServerErrorException;

public class JaxRsHttpClientEndpointRequestExecutorTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private JaxRsHttpClientEndpointRequestExecutor executor;

	private MockServerClient mockServerClient;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		executor = new JaxRsHttpClientEndpointRequestExecutor();
	}

	@Test
	public void shouldSendGetRequestOnJsonFormat() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\": \"Tiago de Freitas Lima\",\"age\":31}")));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/json"), "GET", MyModel.class);

		EndpointResponse<MyModel> myModelResponse = executor.execute(endpointRequest);

		assertTrue(myModelResponse.status().isOk());

		MyModel myModel = myModelResponse.body();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);
	}

	@Test
	public void shouldSendPostRequestOnJsonFormat() {
		HttpRequest httpRequest = request()
			.withMethod("POST")
			.withPath("/json")
			.withHeader("Content-Type", "application/json")
			.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"age\":31}"));

		mockServerClient
			.when(httpRequest)
				.respond(response()
						.withStatusCode(201)
						.withHeader("Content-Type", "text/plain")
						.withBody(exact("OK")));

		MyModel myModel = new MyModel("Tiago de Freitas Lima", 31);

		Headers headers = new Headers()
				.add(new Header("Content-Type", "application/json"));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/json"), "POST", headers,
				myModel, String.class);

		EndpointResponse<String> myModelResponse = executor.execute(endpointRequest);

		assertTrue(myModelResponse.status().isCreated());

		assertEquals("OK", myModelResponse.body());

		mockServerClient.verify(httpRequest, once());
	}

	@Test
	public void shouldSendGetRequestOnXmlFormat() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/xml"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/xml")
					.withBody(exact("<model><name>Tiago de Freitas Lima</name><age>31</age></model>")));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/xml"), "GET", MyModel.class);

		EndpointResponse<MyModel> myModelResponse = executor.execute(endpointRequest);

		assertTrue(myModelResponse.status().isOk());

		MyModel myModel = myModelResponse.body();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);
	}

	@Test
	public void shouldSendPostRequestOnXmlFormat() {
		HttpRequest httpRequest = request()
			.withMethod("POST")
			.withPath("/xml")
			.withHeader("Content-Type", "application/xml")
			.withBody(exact("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><model><name>Tiago de Freitas Lima</name><age>31</age></model>"));

		mockServerClient
			.when(httpRequest)
				.respond(response()
						.withStatusCode(201)
						.withHeader("Content-Type", "text/plain")
						.withBody(exact("OK")));

		MyModel myModel = new MyModel("Tiago de Freitas Lima", 31);

		Headers headers = new Headers()
				.add(new Header("Content-Type", "application/xml"));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/xml"), "POST", headers,
				myModel, String.class);

		EndpointResponse<String> myModelResponse = executor.execute(endpointRequest);

		assertTrue(myModelResponse.status().isCreated());

		assertEquals("OK", myModelResponse.body());

		mockServerClient.verify(httpRequest, once());
	}

	@Test(expected = EndpointResponseInternalServerErrorException.class)
	public void shouldReadServerErrorResponse() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(500));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/json"), "GET", MyModel.class);

		executor.execute(endpointRequest);
	}

	@Test
	public void shouldReturnNullBodyWhenResponseIsNotFound() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(404));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/json"), "GET", MyModel.class);

		EndpointResponse<Object> endpointResponse = executor.execute(endpointRequest);

		assertTrue(endpointResponse.status().isNotFound());
		assertNull(endpointResponse.body());
	}

	@XmlRootElement(name = "model")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class MyModel {

		@JsonProperty
		String name;

		@JsonProperty
		int age;

		public MyModel() {
		}

		public MyModel(@JsonProperty String name, @JsonProperty int age) {
			this.name = name;
			this.age = age;
		}
	}
}
