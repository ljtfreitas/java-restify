package com.github.ljtfreitas.restify.http.client.hateoas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HypermediaResourceTest {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	@Test
	public void resourceMustBeSerializedToJsonOnWebLinkFormat() throws IOException {
		Resource<Model> resource = new Resource<>(new Model("Tiago de Freitas Lima"));
		resource.addLink(Link.self("http://my.api/me"));
		resource.addLink(new Link("http://my.api/me/friends", "friends"));

		String expected = "{\"name\":\"Tiago de Freitas Lima\","
				+ "\"links\":["
					+ "{\"href\":\"http://my.api/me\",\"rel\":\"self\"},"
					+ "{\"href\":\"http://my.api/me/friends\",\"rel\":\"friends\"}"
				+ "]}";

		String output = objectMapper.writeValueAsString(resource);

		assertEquals(expected, output);
	}

	@Test
	public void resourceMustBeSerializedToJsonOnWebLinkFormatWithAttributes() throws IOException {
		Resource<Model> resource = new Resource<>(new Model("Tiago de Freitas Lima"));
		resource.addLink(Link.self("http://my.api"));
		resource.addLink(new LinkBuilder()
				.href("http://my.api/{user}/friends")
				.rel("friends")
				.title("All friends for user")
				.build());

		String expected = "{\"name\":\"Tiago de Freitas Lima\","
				+ "\"links\":["
					+ "{\"href\":\"http://my.api\",\"rel\":\"self\"},"
					+ "{\"href\":\"http://my.api/{user}/friends\",\"rel\":\"friends\",\"title\":\"All friends for user\"}"
				+ "]}";

		String output = objectMapper.writeValueAsString(resource);

		assertEquals(expected, output);
	}

	@Test
	public void resourceMustBeDeserializedFromJsonOnWebLinkFormat() throws IOException {
		String source = "{\"name\":\"Tiago de Freitas Lima\","
				+ "\"links\":["
					+ "{\"href\":\"http://my.api/me\",\"rel\":\"self\"},"
					+ "{\"href\":\"http://my.api/me/friends\",\"rel\":\"friends\"}"
				+ "]}";

		Resource<Model> resource = objectMapper.readValue(source, new TypeReference<Resource<Model>>(){});

		assertNotNull(resource);

		assertEquals(2, resource.links().size());

		Optional<Link> self = resource.links().self();
		assertTrue(self.isPresent());
		assertEquals("http://my.api/me", self.get().href());

		Optional<Link> friends = resource.links().get("friends");
		assertTrue(friends.isPresent());
		assertEquals("http://my.api/me/friends", friends.get().href());
	}


	@Test
	public void resourceMustBeDeserializedFromJsonOnWebLinkFormatWithAttributes() throws IOException {
		String source = "{\"name\":\"Tiago de Freitas Lima\","
				+ "\"links\":["
					+ "{\"href\":\"http://my.api/tiago\",\"rel\":\"self\"},"
					+ "{\"href\":\"http://my.api/{name}/friends\",\"rel\":\"friends\",\"templated\":true,\"title\":\"All friends for user\"}"
				+ "]}";

		Resource<Model> resource = objectMapper.readValue(source, new TypeReference<Resource<Model>>(){});

		assertNotNull(resource);

		assertEquals(2, resource.links().size());

		Optional<Link> self = resource.links().self();
		assertTrue(self.isPresent());
		assertEquals("http://my.api/tiago", self.get().href());

		Optional<Link> friends = resource.links().get("friends");
		assertTrue(friends.isPresent());
		assertEquals("http://my.api/{name}/friends", friends.get().href());
		assertEquals("All friends for user", friends.get().title().get());
		assertEquals("true", friends.get().property("templated").get());
	}

	private static class Model {

		@JsonProperty
		String name;

		Model(@JsonProperty("name") String name) {
			this.name = name;
		}
	}

}
