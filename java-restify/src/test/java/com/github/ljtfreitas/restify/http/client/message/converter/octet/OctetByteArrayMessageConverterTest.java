package com.github.ljtfreitas.restify.http.client.message.converter.octet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.SimpleHttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.SimpleHttpResponseMessage;

public class OctetByteArrayMessageConverterTest {

	private OctetByteArrayMessageConverter converter;

	@Before
	public void setup() {
		converter = new OctetByteArrayMessageConverter();
	}

	@Test
	public void shouldCanReadWhenTypeIsByteArray() {
		assertTrue(converter.canRead(byte[].class));
	}

	@Test
	public void shouldNotCanReadWhenTypeNotIsByteArray() {
		assertFalse(converter.canRead(String.class));
	}

	@Test
	public void shouldReadHttpResponseToByteArray() {
		String body = "response";

		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream(body.getBytes()));

		byte[] byteArray = converter.read(httpResponseMessage, InputStream.class);

		String output = new String(byteArray);

		assertEquals(body, output);
	}

	@Test
	public void shouldCanWriteWhenTypeIsByteArray() {
		assertTrue(converter.canWrite(byte[].class));
	}

	@Test
	public void shouldNotCanWriteWhenTypeNotIsByteArray() {
		assertFalse(converter.canWrite(String.class));
	}

	@Test
	public void shouldWriteByteArrayBodyToOutputStream() {
		String body = "request body";

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		HttpRequestMessage httpRequestMessage = new SimpleHttpRequestMessage(outputStream);

		converter.write(body.getBytes(), httpRequestMessage);

		String output = new String(outputStream.toByteArray());

		assertEquals(body, output);
	}
}
