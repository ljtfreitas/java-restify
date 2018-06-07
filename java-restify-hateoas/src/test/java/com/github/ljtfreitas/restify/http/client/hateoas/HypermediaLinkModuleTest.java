package com.github.ljtfreitas.restify.http.client.hateoas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.HypermediaBrowserBuilder;

public class HypermediaLinkModuleTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	private MockServerClient mockServerClient;

	@Before
	public void before() {
		mockServerClient = new MockServerClient("localhost", 7080);

		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/address"))
			.respond(response()
				.withStatusCode(200)
				.withHeader("Content-Type", "application/json")
				.withBody(json("{\"street\":\"Rua Ester Samara\",\"city\":\"Sao Paulo\"}")));
	}

	@Test
	public void shouldDeserializeLink() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new HypermediaLinkModule(new HypermediaBrowserBuilder().build()));

		Person person = objectMapper.readValue("{\"name\":\"Tiago de Freitas Lima\",\"address\":\"http://localhost:7080/address\"}", Person.class);

		assertNotNull(person.whatever);
		assertEquals("address", person.whatever.rel());

		Address address = person.whatever.follow().as(Address.class);
		assertEquals("Rua Ester Samara", address.street);
		assertEquals("Sao Paulo", address.city);
	}

	private static class Person {

		@JsonProperty
		String name;

		@JsonProperty("address")
		Link whatever;
	}

	private static class Address {

		@JsonProperty
		String street;

		@JsonProperty
		String city;
	}
}
