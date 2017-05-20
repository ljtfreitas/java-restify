package com.github.ljtfreitas.restify.http.client.message.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.SimpleHttpResponseMessage;

public class ByteArrayMessageConverterTest {

	private ByteArrayMessageConverter converter;

	@Before
	public void setup() {
		converter = new ByteArrayMessageConverter();
	}

	@Test
	public void shouldCanReadByteArrayType() {
		assertTrue(converter.canRead(byte[].class));
	}

	@Test
	public void shouldNotCanReadTypeThatIsNotByteArray() {
		assertFalse(converter.canRead(String.class));
	}

	@Test
	public void shouldConvertHttpResponseMessageBodyToByteArray() {
		String body = "hello world";

		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream(body.getBytes()));

		byte[] byteArray = converter.read(httpResponseMessage, byte[].class);

        String output = new String(byteArray);

		assertEquals(body, output);
	}
}
