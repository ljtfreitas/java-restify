package com.github.ljtfreitas.restify.http.client.message.converter.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.ContentType;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class StringMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock
	private HttpResponseMessage response;

	private StringMessageConverter converter;

	private String message;

	@Before
	public void setup() {
		converter = new StringMessageConverter() {
			@Override
			public ContentType contentType() {
				return ContentType.of("*");
			}
		};

		message = "Simple String message.\nSimple String message, second line.";

		when(request.charset())
			.thenReturn(Charset.forName("UTF-8"));
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
		RequestBody output = new RequestBody();

		when(request.body()).thenReturn(output);

		converter.write(message, request);

		assertEquals(message, output.toString());
	}

	@Test
	public void shouldReadStringMessage() {
		ByteArrayInputStream input = new ByteArrayInputStream(message.getBytes());

		when(response.body()).thenReturn(input);

		Object content = converter.read(response, String.class);

		assertEquals(message, content);
	}
}
