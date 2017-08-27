package com.github.ljtfreitas.restify.http.client.hateoas.hal;

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
import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.hateoas.LinkBuilder;
import com.github.ljtfreitas.restify.http.client.hateoas.Resource;

public class JsonHypermediaHalResourceTest {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new HypermediaHalModule());
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.setHandlerInstantiator(new HypermediaHalHandlerInstantiator(null));
	}

	@Test
	public void resourceMustBeSerializedToJsonOnHalFormat() throws IOException {
		Resource<Model> resource = new Resource<>(new Model("Tiago de Freitas Lima"));
		resource.addLink(Link.self("http://my.api/me"));
		resource.addLink(new Link("http://my.api/me/friends", "friends"));

		String expected = "{\"name\":\"Tiago de Freitas Lima\","
				+ "\"_links\":{"
					+ "\"self\":{\"href\":\"http://my.api/me\"},"
					+ "\"friends\":{\"href\":\"http://my.api/me/friends\"}"
				+ "}}";

		String output = objectMapper.writeValueAsString(resource);

		assertEquals(expected, output);
	}

	@Test
	public void resourceMustBeSerializedToJsonOnHalFormatWithAttributes() throws IOException {
		Resource<Model> resource = new Resource<>(new Model("Tiago de Freitas Lima"));
		resource.addLink(Link.self("http://my.api"));
		resource.addLink(new LinkBuilder()
				.href("http://my.api/{user}/friends")
				.rel("friends")
				.title("All friends for user")
				.templated()
				.build());

		String expected = "{\"name\":\"Tiago de Freitas Lima\","
				+ "\"_links\":{"
					+ "\"self\":{\"href\":\"http://my.api\"},"
					+ "\"friends\":{"
						+ "\"href\":\"http://my.api/{user}/friends\",\"templated\":true,\"title\":\"All friends for user\""
					+ "}"
				+ "}}";

		String output = objectMapper.writeValueAsString(resource);

		assertEquals(expected, output);
	}

	@Test
	public void resourceMustBeDeserializedFromJsonOnHalFormat() throws IOException {
		String source = "{\"name\":\"Tiago de Freitas Lima\","
				+ "\"_links\":{"
					+ "\"self\":{\"href\":\"http://my.api/me\"},"
					+ "\"friends\":{\"href\":\"http://my.api/me/friends\"}"
				+ "}}";

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
	public void resourceMustBeDeserializedFromJsonOnHalFormatWithAttributes() throws IOException {
		String source = "{\"name\":\"Tiago de Freitas Lima\","
				+ "\"_links\":{"
					+ "\"self\":{\"href\":\"http://my.api/tiago\"},"
					+ "\"friends\":{\"href\":\"http://my.api/{name}/friends\",\"templated\":true,\"title\":\"All friends for user\"}"
				+ "}}";

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
