package com.restify.http.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

		when(httpMessageReaderMock.read(eq(String.class), any())).thenReturn(endpointResult);
	}

	@Test
	public void shouldReadSuccessResponse() {
		endpointResponse = new SimpleEndpointResponse(new EndpointResponseCode(200), 
				new ByteArrayInputStream(endpointResult.getBytes()));

		Object result = endpointResponseReader.read(endpointResponse, String.class);

		assertEquals(endpointResult, result);
	}

	@Test(expected = RestifyHttpException.class)
	public void shouldThrowExceptionWhenResponseStatusCodeIsError() {
		endpointResponse = new SimpleEndpointResponse(new EndpointResponseCode(500));

		endpointResponseReader.read(endpointResponse, String.class);
	}

	private class SimpleEndpointResponse extends EndpointResponse {

		private final InputStream stream;

		public SimpleEndpointResponse(EndpointResponseCode code) {
			this(code, new ByteArrayInputStream(new byte[0]));
		}

		public SimpleEndpointResponse(EndpointResponseCode code, InputStream stream) {
			super(code, new Headers(), stream);
			this.stream = stream;
		}

		public SimpleEndpointResponse(EndpointResponseCode code, Headers headers, InputStream stream) {
			super(code, headers, stream);
			this.stream = stream;
		}

		@Override
		public void close() throws IOException {
			stream.close();
		}
	}

}
