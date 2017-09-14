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
import org.mockserver.model.HttpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.HypermediaBrowser;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.HypermediaBrowserBuilder;

public class LinkTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	private MockServerClient mockServerClient;

	private Link link;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		HttpRequest httpRequest = request()
				.withMethod("GET")
				.withPath("/me");

		mockServerClient.when(httpRequest)
			.respond(
					response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/hal+json")
					.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\"}")));

		HypermediaBrowser hypermediaBrowser = new HypermediaBrowserBuilder().build();

		link = Link.self("http://localhost:7080/me", hypermediaBrowser);
	}

	@Test
	public void shouldFollowLink() {
		Person person = link.follow().as(Person.class);

		assertNotNull(person);
		assertEquals("Tiago de Freitas Lima", person.name);
		assertNotNull("1985-07-02", person.birthDate);
	}

	private static class Person {

		private final String name;
		private final String birthDate;

		private Person(@JsonProperty("name") String name, @JsonProperty("birth_date") String birthDate) {
			this.name = name;
			this.birthDate = birthDate;
		}
	}

}
