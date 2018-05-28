package com.github.ljtfreitas.restify.http.client.message.converter.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class JsonpMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock
	private HttpResponseMessage response;

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
	public void shouldCanWriteJsonObject() {
		assertTrue(converter.canWrite(JsonObject.class));
	}

	@Test
	public void shouldWriteJsonObject() {
		RequestBody output = new RequestBody();

		when(request.body()).thenReturn(output);

		converter.write(jsonObject, request);

		assertEquals(jsonObject.toString(), output.toString());
	}

	@Test
	public void shouldCanReadJsonObject() {
		assertTrue(converter.canRead(JsonObject.class));
	}

	@Test
	public void shouldReadJsonObject() {
		ByteArrayInputStream input = new ByteArrayInputStream(jsonObject.toString().getBytes());

		when(response.body()).thenReturn(input);
		
		JsonObject object = (JsonObject) converter.read(response, JsonObject.class);

		assertEquals("Tiago de Freitas Lima", object.getString("name"));
		assertEquals(31, object.getInt("age"));
	}

	@Test
	public void shouldCanWriteJsonArray() {
		assertTrue(converter.canWrite(JsonArray.class));
	}

	@Test
	public void shouldWriteJsonArray() {
		RequestBody output = new RequestBody();

		when(request.body()).thenReturn(output);

		converter.write(jsonArray, request);

		assertEquals(jsonArray.toString(), output.toString());
	}

	@Test
	public void shouldCanReadJsonArray() {
		assertTrue(converter.canRead(JsonArray.class));
	}

	@Test
	public void shouldReadJsonArray() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(jsonArray.toString().getBytes());

		when(response.body()).thenReturn(input);

		JsonArray array = (JsonArray) converter.read(response, JsonArray.class);

		assertEquals(2, array.size());

		JsonObject jsonArrayElement = array.getJsonObject(0);
		assertEquals("Tiago de Freitas Lima 1", jsonArrayElement.getString("name"));
		assertEquals(31, jsonArrayElement.getInt("age"));

		jsonArrayElement = array.getJsonObject(1);
		assertEquals("Tiago de Freitas Lima 2", jsonArrayElement.getString("name"));
		assertEquals(32, jsonArrayElement.getInt("age"));
	}

	@Test
	public void shouldNotCanWriteWhenTypeIsNotJsonObjectOrJsonArray() {
		assertFalse(converter.canWrite(String.class));
	}

	@Test
	public void shouldNotCanReadWhenTypeIsNotJsonObjectOrJsonArray() {
		assertFalse(converter.canRead(String.class));
	}
}
