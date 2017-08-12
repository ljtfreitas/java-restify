package com.github.ljtfreitas.restify.http.client.hateoas.hal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.hateoas.Resource;

public class HalResourceTest {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JacksonHypermediaHalModule());
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
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
	public void resourceMustBeDeserializedFromJsonOnHalFormat() throws IOException {
		String source = "{\"name\":\"Tiago de Freitas Lima\","
				+ "\"_links\":{"
					+ "\"self\":{\"href\":\"http://my.api/me\"},"
					+ "\"friends\":{\"href\":\"http://my.api/me/friends\"}"
				+ "}}";

		Resource<Model> resource = objectMapper.readValue(source, new TypeReference<Resource<Model>>(){});

		assertNotNull(resource);
	}

	private static class Model {

		@JsonProperty
		String name;

		Model(@JsonProperty("name") String name) {
			this.name = name;
		}
	}

}
