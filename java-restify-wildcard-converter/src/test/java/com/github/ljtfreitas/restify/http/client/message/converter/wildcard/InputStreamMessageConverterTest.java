package com.github.ljtfreitas.restify.http.client.message.converter.wildcard;

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

import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class InputStreamMessageConverterTest {

	@Mock
	private HttpResponseMessage response;

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

		when(response.body()).thenReturn(new ByteArrayInputStream(body.getBytes()));

		InputStream stream = converter.read(response, InputStream.class);

		BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
        String output = buffer.lines().collect(Collectors.joining("\n"));

		assertEquals(body, output);
	}
}
