package com.github.ljtfreitas.restify.http.client.message.response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.io.InputStreamContent;

@RunWith(MockitoJUnitRunner.class)
public class ByteArrayHttpResponseBodyTest {

	@Mock
	private HttpResponseBody source;

	private ByteArrayHttpResponseBody subject;

	@Before
	public void setup() {
		when(source.input())
			.thenReturn(new ByteArrayInputStream("this is a response".getBytes()));

		subject = ByteArrayHttpResponseBody.of(source);
	}

	@Test
	public void shouldConvertResponseBodyToString() {
		assertEquals("this is a response", subject.asString());
	}

	@Test
	public void shouldReadMultiplesTimesFromInputStream() throws IOException {
		assertEquals("this is a response", new InputStreamContent(subject.input()).asString());
		assertEquals("this is a response", new InputStreamContent(subject.input()).asString());
		assertEquals("this is a response", new InputStreamContent(subject.input()).asString());
	}
}
