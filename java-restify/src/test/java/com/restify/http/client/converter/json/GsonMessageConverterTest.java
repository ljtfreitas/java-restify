package com.restify.http.client.converter.json;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.converter.SimpleHttpRequestMessage;
import com.restify.http.client.converter.SimpleHttpResponseMessage;

public class GsonMessageConverterTest {

	private GsonMessageConverter converter = new GsonMessageConverter();

	private String json;

	@Before
	public void setup() {
		StringBuilder sb = new StringBuilder();
		sb.append("{")
			.append("\"name\":\"Tiago de Freitas Lima\",")
			.append("\"age\":31")
			.append("}");

		json = sb.toString();
	}

	@Test
	public void shouldWriteJsonMessage() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		converter.write(new MyJsonModel("Tiago de Freitas Lima", 31), new SimpleHttpRequestMessage(output));

		assertEquals(json, output.toString());
	}

	@Test
	public void shouldReadJsonMessage() {
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes());

		MyJsonModel myJsonModel = (MyJsonModel) converter.read(MyJsonModel.class, new SimpleHttpResponseMessage(input));

		assertEquals("Tiago de Freitas Lima", myJsonModel.name);
		assertEquals(31, myJsonModel.age);
	}

}
