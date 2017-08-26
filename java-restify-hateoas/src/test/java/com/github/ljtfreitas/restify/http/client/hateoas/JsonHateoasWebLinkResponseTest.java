package com.github.ljtfreitas.restify.http.client.hateoas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JacksonMessageConverter;
import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Path;

public class JsonHateoasWebLinkResponseTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080, 7084);

	private MyApi myApi;

	private MockServerClient mockServerClient;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		JacksonMessageConverter<Object> jacksonMessageConverter = new JacksonMessageConverter<>(objectMapper);

		myApi = new RestifyProxyBuilder().converters(jacksonMessageConverter)
				.target(MyApi.class, "http://localhost:7080").build();
	}

	@Test
	public void shouldSendGetRequestWithHalLinks() {
		mockServerClient.when(request().withMethod("GET").withPath("/json"))
			.respond(response()
						.withStatusCode(200)
						.withHeader("Content-Type", "application/json")
						.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\","
									+ "\"links\":["
										+ "{\"rel\":\"self\",\"href\":\"http://localhost:8080/\"},"
										+ "{\"rel\":\"friends\",\"href\":\"http://localhost:8080/{user}/friends\",\"templated\":true}"
									+ "]}")));

		Resource<MyModel> resource = myApi.json();

		MyModel myModel = resource.content();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals("1985-07-02", myModel.birthDate);

		assertEquals(2, resource.links().size());

		Optional<Link> self = resource.links().self();
		assertTrue(self.isPresent());
		assertEquals("http://localhost:8080/", self.get().href());

		Optional<Link> friends = resource.links().get("friends");
		assertTrue(friends.isPresent());
		assertEquals("http://localhost:8080/{user}/friends", friends.get().href());
	}

	interface MyApi {

		@Path("/json")
		@Get
		public Resource<MyModel> json();

	}

	public static class MyModel {

		@JsonProperty
		String name;

		@JsonProperty("birth_date")
		String birthDate;

		public MyModel(@JsonProperty("name") String name, @JsonProperty("birth_date") String birthDate) {
			this.name = name;
			this.birthDate = birthDate;
		}
	}
}
