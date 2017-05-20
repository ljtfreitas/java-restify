package com.github.ljtfreitas.restify.http.client.message.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.SimpleHttpResponseMessage;

public class InputStreamMessageConverterTest {

	private InputStreamMessageConverter converter;

	@Before
	public void setup() {
		converter = new InputStreamMessageConverter();
	}

	@Test
	public void shouldCanReadInputStreamType() {
		assertTrue(converter.canRead(InputStream.class));
	}

	@Test
	public void shouldNotCanReadTypeThatIsNotInputStream() {
		assertFalse(converter.canRead(String.class));
	}

	@Test
	public void shouldConvertHttpResponseMessageBodyToInputStream() {
		String body = "hello world";

		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream(body.getBytes()));

		InputStream stream = converter.read(httpResponseMessage, InputStream.class);

		BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
        String output = buffer.lines().collect(Collectors.joining("\n"));

		assertEquals(body, output);
	}
}
