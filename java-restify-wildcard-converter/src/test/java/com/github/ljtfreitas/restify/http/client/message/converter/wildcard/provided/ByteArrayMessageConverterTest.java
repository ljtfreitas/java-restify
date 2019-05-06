package com.github.ljtfreitas.restify.http.client.message.converter.wildcard.provided;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.converter.wildcard.provided.ByteArrayMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class ByteArrayMessageConverterTest {

	@Mock
	private HttpResponseMessage response;

	@Mock
	private HttpResponseBody responseBody;

	private ByteArrayMessageConverter converter;

	@Before
	public void setup() {
		when(response.body())
			.thenReturn(responseBody);

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

		when(responseBody.input()).thenReturn(new ByteArrayInputStream(body.getBytes()));

		byte[] byteArray = converter.read(response, byte[].class);

        String output = new String(byteArray);

		assertEquals(body, output);
	}
}
