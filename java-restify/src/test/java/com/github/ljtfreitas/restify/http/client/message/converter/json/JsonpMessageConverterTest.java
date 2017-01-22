package com.github.ljtfreitas.restify.http.client.message.converter.json;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.request.SimpleHttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.response.SimpleHttpResponseMessage;

public class JsonpMessageConverterTest {

	private JsonpMessageConverter converter = new JsonpMessageConverter();

	private JsonObject jsonObject;

	private JsonArray jsonArray;

	@Before
	public void setup() {
		jsonObject = Json.createObjectBuilder()
			.add("name", "Tiago de Freitas Lima")
			.add("age", 31)
			.build();

		jsonArray = Json.createArrayBuilder()
			.add(Json.createObjectBuilder()
					.add("name", "Tiago de Freitas Lima 1")
					.add("age", 31)
					.build())
			.add(Json.createObjectBuilder()
				.add("name", "Tiago de Freitas Lima 2")
				.add("age", 32)
				.build())
			.build();
	}

	@Test
	public void shouldWriteJsonObject() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		converter.write(jsonObject, new SimpleHttpRequestMessage(output));

		assertEquals(jsonObject.toString(), output.toString());
	}

	@Test
	public void shouldReadJsonObject() {
		ByteArrayInputStream input = new ByteArrayInputStream(jsonObject.toString().getBytes());

		JsonObject response = (JsonObject) converter.read(new SimpleHttpResponseMessage(input), JsonObject.class);

		assertEquals("Tiago de Freitas Lima", response.getString("name"));
		assertEquals(31, response.getInt("age"));
	}

	@Test
	public void shouldWriteJsonArray() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		converter.write(jsonArray, new SimpleHttpRequestMessage(output));

		assertEquals(jsonArray.toString(), output.toString());
	}

	@Test
	public void shouldReadJsonArray() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(jsonArray.toString().getBytes());

		JsonArray response = (JsonArray) converter.read(new SimpleHttpResponseMessage(input), JsonArray.class);

		assertEquals(2, response.size());

		JsonObject jsonArrayElement = response.getJsonObject(0);
		assertEquals("Tiago de Freitas Lima 1", jsonArrayElement.getString("name"));
		assertEquals(31, jsonArrayElement.getInt("age"));

		jsonArrayElement = response.getJsonObject(1);
		assertEquals("Tiago de Freitas Lima 2", jsonArrayElement.getString("name"));
		assertEquals(32, jsonArrayElement.getInt("age"));
	}
}
