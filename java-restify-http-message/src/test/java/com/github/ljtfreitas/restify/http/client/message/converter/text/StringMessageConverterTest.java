package com.github.ljtfreitas.restify.http.client.message.converter.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.ContentType;
import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedByteArrayHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class StringMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private HttpResponseMessage response;

	private StringMessageConverter converter;

	private String message;

	private BufferedByteArrayHttpRequestBody output;

	@Before
	public void setup() {
		converter = new StringMessageConverter() {
			@Override
			public Collection<ContentType> contentTypes() {
				return Arrays.asList(ContentType.of("*"));
			}
		};

		message = "Simple String message.\nSimple String message, second line.";

		when(request.charset())
			.thenReturn(Charset.forName("UTF-8"));

		output = new BufferedByteArrayHttpRequestBody();
		
		when(request.body()).thenReturn(output);
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
		converter.write(message, request);

		assertEquals(message, new String(output.asBytes()));
	}

	@Test
	public void shouldWriteStringMessageUsingOtherCharset() throws Exception {
		message = new String("Hellóòú".getBytes("UTF-8"));

		when(request.charset())
			.thenReturn(Encoding.ISO_8859_1.charset());
		
		converter.write(message, request);

		assertEquals(new String(message.getBytes("ISO-8859-1")),
				new String(output.asBytes()));
	}

	@Test
	public void shouldReadStringMessage() {
		ByteArrayInputStream input = new ByteArrayInputStream(message.getBytes());

		when(response.body().input()).thenReturn(input);

		Object content = converter.read(response, String.class);

		assertEquals(message, content);
	}
}
