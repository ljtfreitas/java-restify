package com.github.ljtfreitas.restify.http.client.message.converter.octet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class OctetByteArrayMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private HttpResponseMessage response;

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

		when(response.body().input()).thenReturn(new ByteArrayInputStream(body.getBytes()));

		byte[] byteArray = converter.read(response, InputStream.class);

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

		HttpRequestBody buffer = new BufferedHttpRequestBody();
		when(request.body()).thenReturn(buffer);

		converter.write(body.getBytes(), request);

		String output = buffer.asString();

		assertEquals(body, output);
	}
}
