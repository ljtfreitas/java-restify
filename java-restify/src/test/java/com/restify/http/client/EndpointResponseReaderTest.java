package com.restify.http.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.converter.HttpMessageReader;
import com.restify.http.contract.ContentType;
import com.restify.http.metadata.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class EndpointResponseReaderTest {

	@Mock
	private HttpMessageConverters httpMessageConvertersMock;

	@Mock
	private HttpMessageReader<Object> httpMessageReaderMock;

	@InjectMocks
	private EndpointResponseReader endpointResponseReader;

	private String endpointResult;

	private EndpointResponse endpointResponse;

	@Before
	public void setup() {
		endpointResult = "expected result";

		when(httpMessageConvertersMock.readerOf(ContentType.of("text/plain"), String.class))
				.thenReturn(Optional.of(httpMessageReaderMock));

		when(httpMessageReaderMock.read(any(), eq(String.class))).thenReturn(endpointResult);
	}

	@Test
	public void shouldReadSuccessResponse() {
		endpointResponse = new SimpleEndpointResponse(new ByteArrayInputStream(endpointResult.getBytes()));

		Object result = endpointResponseReader.read(endpointResponse, EndpointExpectedType.of(String.class));

		assertEquals(endpointResult, result);
	}

	@Test(expected = RestifyHttpException.class)
	public void shouldThrowExceptionWhenResponseStatusCodeIsError() {
		endpointResponse = new SimpleEndpointResponse(EndpointResponseCode.internalServerError());

		endpointResponseReader.read(endpointResponse, EndpointExpectedType.of(String.class));
	}

	@Test
	public void shouldReturnNullWhenExpectedTypeIsVoid() {
		endpointResponse = new SimpleEndpointResponse();

		Object result = endpointResponseReader.read(endpointResponse, EndpointExpectedType.of(Void.class));

		assertNull(result);
	}

	@Test
	public void shouldReturnTheSameEndpointResponseParameterWhenReturnTypeIsEndpointResponse() {
		endpointResponse = new SimpleEndpointResponse();

		Object result = endpointResponseReader.read(endpointResponse, EndpointExpectedType.of(EndpointResponse.class));

		assertSame(endpointResponse, result);
	}

	@Test
	public void shouldReturnTheSameEndpointResponseHeadersOfParameterWhenReturnTypeIsHeaders() {
		Headers responseHeaders = new Headers();

		endpointResponse = new SimpleEndpointResponse(responseHeaders);

		Object result = endpointResponseReader.read(endpointResponse, EndpointExpectedType.of(Headers.class));

		assertSame(responseHeaders, result);
	}

	@Test
	public void shouldReturnNullWhenEndpointResponseCodeIsNotReadable() {
		endpointResponse = new SimpleEndpointResponse(EndpointResponseCode.noContent());

		Object result = endpointResponseReader.read(endpointResponse, EndpointExpectedType.of(String.class));

		assertNull(result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnTypedResponseObjectWhenResponseTypeIsExpected() {
		endpointResponse = new SimpleEndpointResponse(new ByteArrayInputStream(endpointResult.getBytes()));

		Type expectedType = new SimpleParameterizedType(Response.class, null, String.class);

		Response<String> result = (Response<String>) endpointResponseReader.read(endpointResponse, EndpointExpectedType.of(expectedType));

		assertEquals(endpointResult, result.body());
	}

	private class SimpleEndpointResponse extends EndpointResponse {

		private final InputStream stream;

		private SimpleEndpointResponse(EndpointResponseCode code, Headers headers, InputStream stream) {
			super(code, headers, stream);
			this.stream = stream;
		}

		public SimpleEndpointResponse() {
			this(EndpointResponseCode.ok(), new Headers(), new ByteArrayInputStream(new byte[0]));
		}

		public SimpleEndpointResponse(EndpointResponseCode code) {
			this(code, new Headers(), new ByteArrayInputStream(new byte[0]));
		}

		public SimpleEndpointResponse(Headers headers) {
			this(EndpointResponseCode.ok(), headers, new ByteArrayInputStream(new byte[0]));
		}

		public SimpleEndpointResponse(InputStream stream) {
			this(EndpointResponseCode.ok(), new Headers(), stream);
		}

		@Override
		public void close() throws IOException {
			stream.close();
		}
	}

}
