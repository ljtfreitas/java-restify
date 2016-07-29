package com.restify.http.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;

public class EndpointResponseReaderTest {

	@Test
	public void shouldReadSuccessResponse() {
		EndpointResponseReader reader = new EndpointResponseReader(new HttpMessageConverters(new SimpleHttpMessageConverter()));

		InputStream stream = new ByteArrayInputStream("expected result".getBytes());

		EndpointResponse response = new EndpointResponse(new EndpointResponseCode(200), new Headers(), stream) {
			@Override
			public void close() throws IOException {
				stream.close();
			}
		};

		Object result = reader.ifSuccess(response.code(), r -> r.read(response, String.class)).get();

		assertEquals("expected result", result);
	}

	@Test
	public void shouldNotReadResponseWhenResponseCodeIsNotSuccess() {
		EndpointResponseReader reader = new EndpointResponseReader(new HttpMessageConverters(new SimpleHttpMessageConverter()));

		Optional<Object> result = reader.ifSuccess(new EndpointResponseCode(400), null);

		assertFalse(result.isPresent());
	}

	@Test
	public void shouldReadErrorResponse() {
		EndpointResponseReader reader = new EndpointResponseReader(new HttpMessageConverters(new SimpleHttpMessageConverter()));

		InputStream stream = new ByteArrayInputStream("error message".getBytes());

		EndpointResponse response = new EndpointResponse(new EndpointResponseCode(500), new Headers(), stream) {
			@Override
			public void close() throws IOException {
				stream.close();
			}
		};

		RestifyHttpException e = reader.onError(response);

		assertEquals("500 error message", e.getMessage());

	}

	private class SimpleHttpMessageConverter implements HttpMessageConverter<Object> {

		@Override
		public String contentType() {
			return "text/plain";
		}

		@Override
		public boolean canWrite(Class<?> type) {
			return true;
		}

		@Override
		public void write(Object body, HttpRequestMessage httpRequestMessage) {
		}

		@Override
		public boolean canRead(Class<?> type) {
			return true;
		}

		@Override
		public Object read(Class<?> expectedType, HttpResponseMessage httpResponseMessage) {
			try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponseMessage.input()))) {
				return buffer.lines().collect(Collectors.joining("\n"));

			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
