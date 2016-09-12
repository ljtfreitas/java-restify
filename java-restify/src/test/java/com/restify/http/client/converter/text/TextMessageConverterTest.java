package com.restify.http.client.converter.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.converter.SimpleHttpRequestMessage;
import com.restify.http.client.converter.SimpleHttpResponseMessage;

public class TextMessageConverterTest {

	private TextMessageConverter converter;

	private String message;

	@Before
	public void setup() {
		converter = new TextMessageConverter() {
			@Override
			public String contentType() {
				return "*";
			}
		};

		message = "Simple String message.\nSimple String message, second line.";
	}

	@Test
	public void shouldCanWriteStringType() {
		assertTrue(converter.canWrite(String.class));
	}

	@Test
	public void shouldNotCanWriteTypesTharAreNotString() {
		assertFalse(converter.canRead(Object.class));
	}

	@Test
	public void shouldCanReadStringType() {
		assertTrue(converter.canRead(String.class));
	}

	@Test
	public void shouldNotCanReadTypesTharAreNotString() {
		assertFalse(converter.canRead(Object.class));
	}

	@Test
	public void shouldWriteStringMessage() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		converter.write(message, new SimpleHttpRequestMessage(output));

		assertEquals(message, output.toString());
	}

	@Test
	public void shouldReadStringMessage() {
		ByteArrayInputStream input = new ByteArrayInputStream(message.getBytes());

		Object content = converter.read(String.class, new SimpleHttpResponseMessage(input));

		assertEquals(message, content);
	}
}
