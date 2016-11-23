package com.github.ljtfreitas.restify.http.client.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageReader;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;
import com.github.ljtfreitas.restify.http.contract.ContentType;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class EndpointResponseReaderTest {

	@Mock
	private HttpMessageConverters httpMessageConvertersMock;

	@Mock
	private HttpMessageReader<Object> httpMessageReaderMock;

	@Mock
	private EndpointResponseErrorFallback endpointResponseErrorFallbackMock;

	@InjectMocks
	private EndpointResponseReader endpointResponseReader;

	private String endpointResult;

	@Before
	public void setup() {
		endpointResult = "expected result";

		when(httpMessageConvertersMock.readerOf(ContentType.of("text/plain"), String.class))
				.thenReturn(Optional.of(httpMessageReaderMock));

		when(httpMessageReaderMock.read(any(), eq(String.class))).thenReturn(endpointResult);
	}

	@Test
	public void shouldReadSuccessResponse() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream(endpointResult.getBytes()));

		EndpointResponse<String> response = endpointResponseReader.read(httpResponseMessage, JavaType.of(String.class));

		assertEquals(endpointResult, response.body());
	}

	@Test
	public void shouldCallEndpointResponseErorHandlerWhenResponseStatusCodeIsError() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(StatusCode.internalServerError());

		endpointResponseReader.read(httpResponseMessage, JavaType.of(String.class));

		verify(endpointResponseErrorFallbackMock)
			.onError(httpResponseMessage);
	}

	@Test
	public void shouldReturnEmptyResponseWhenJavaTypeIsVoid() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage();

		EndpointResponse<Void> response = endpointResponseReader.read(httpResponseMessage, JavaType.of(Void.class));

		assertNull(response.body());
	}

	@Test
	public void shouldReturnEmptyResponseWhenHttpResponseCodeIsNotReadable() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(StatusCode.noContent());

		EndpointResponse<String> response = endpointResponseReader.read(httpResponseMessage, JavaType.of(String.class));

		assertNull(response.body());
	}
}
