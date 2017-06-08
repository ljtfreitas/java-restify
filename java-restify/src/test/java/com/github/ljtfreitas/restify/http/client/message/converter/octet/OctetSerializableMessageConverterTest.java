package com.github.ljtfreitas.restify.http.client.message.converter.octet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.SimpleHttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.SimpleHttpResponseMessage;

public class OctetSerializableMessageConverterTest {

	private OctetSerializableMessageConverter<String> converter;

	@Before
	public void setup() {
		converter = new OctetSerializableMessageConverter<>();
	}

	@Test
	public void shouldCanReadWhenTypeIsSerializable() {
		assertTrue(converter.canRead(String.class));
	}

	@Test
	public void shouldNotCanReadWhenTypeNotIsSerializable() {
		assertFalse(converter.canRead(this.getClass()));
	}

	@Test
	public void shouldDeserializeHttpResponseToString() throws IOException {
		String body = "response";

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(body);
		objectOutputStream.flush();

		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream(outputStream.toByteArray()));

		String output = converter.read(httpResponseMessage, String.class);

		assertEquals(body, output);
	}

	@Test
	public void shouldCanWriteWhenTypeIsSerializable() {
		assertTrue(converter.canWrite(String.class));
	}

	@Test
	public void shouldNotCanWriteWhenTypeNotIsSerializable() {
		assertFalse(converter.canWrite(this.getClass()));
	}

	@Test
	public void shouldSerializeStringBodyToOutputStream() throws Exception {
		String body = "request body";

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		HttpRequestMessage httpRequestMessage = new SimpleHttpRequestMessage(outputStream);

		converter.write(body, httpRequestMessage);

		ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
		Object output = objectInputStream.readObject();

		assertEquals(body, output);
	}
}
