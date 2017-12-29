package com.github.ljtfreitas.restify.http.client.message.converter.octet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class OctetSerializableMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock
	private HttpResponseMessage response;

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

		when(response.body()).thenReturn(new ByteArrayInputStream(outputStream.toByteArray()));

		String output = converter.read(response, String.class);

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
		when(request.output()).thenReturn(outputStream);

		converter.write(body, request);

		ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
		Object output = objectInputStream.readObject();

		assertEquals(body, output);
	}
}
