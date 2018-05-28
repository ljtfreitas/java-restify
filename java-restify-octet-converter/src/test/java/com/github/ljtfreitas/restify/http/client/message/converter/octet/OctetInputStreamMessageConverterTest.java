package com.github.ljtfreitas.restify.http.client.message.converter.octet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class OctetInputStreamMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock
	private HttpResponseMessage response;

	private OctetInputStreamMessageConverter converter;

	@Before
	public void setup() {
		converter = new OctetInputStreamMessageConverter();
	}

	@Test
	public void shouldCanReadWhenTypeIsInputStream() {
		assertTrue(converter.canRead(InputStream.class));
	}

	@Test
	public void shouldNotCanReadWhenTypeNotIsInputStream() {
		assertFalse(converter.canRead(String.class));
	}

	@Test
	public void shouldReadHttpResponseToInputStream() {
		String body = "response";

		when(response.body()).thenReturn(new ByteArrayInputStream(body.getBytes()));

		InputStream stream = converter.read(response, InputStream.class);

		BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));

		String output = buffer.lines().collect(Collectors.joining("\n"));

		assertEquals(body, output);
	}

	@Test
	public void shouldCanWriteWhenTypeIsInputStream() {
		assertTrue(converter.canWrite(InputStream.class));
	}

	@Test
	public void shouldNotCanWriteWhenTypeNotIsInputStream() {
		assertFalse(converter.canWrite(String.class));
	}

	@Test
	public void shouldWriteInputStreamBodyToOutputStream() {
		String body = "request body";

		RequestBody buffer = new RequestBody();

		when(request.body()).thenReturn(buffer);

		converter.write(new ByteArrayInputStream(body.getBytes()), request);

		String output = new String(buffer.toByteArray());

		assertEquals(body, output);
	}
}
