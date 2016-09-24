package com.restify.http.client.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.Headers;
import com.restify.http.client.message.HttpMessageConverters;
import com.restify.http.client.message.HttpMessageReader;
import com.restify.http.client.request.ExpectedType;
import com.restify.http.contract.ContentType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class EndpointResponseReaderTest {

	@Mock
	private HttpMessageConverters httpMessageConvertersMock;

	@Mock
	private HttpMessageReader<Object> httpMessageReaderMock;

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

		Object result = endpointResponseReader.read(httpResponseMessage, ExpectedType.of(String.class));

		assertEquals(endpointResult, result);
	}

	@Test(expected = RestifyHttpException.class)
	public void shouldThrowExceptionWhenResponseStatusCodeIsError() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(EndpointResponseCode.internalServerError());

		endpointResponseReader.read(httpResponseMessage, ExpectedType.of(String.class));
	}

	@Test
	public void shouldReturnNullWhenExpectedTypeIsVoid() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage();

		Object result = endpointResponseReader.read(httpResponseMessage, ExpectedType.of(Void.class));

		assertNull(result);
	}

	@Test
	public void shouldReturnTheSameHttpResponseMessageWhenReturnTypeIsHttpResponseMessage() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage();

		Object result = endpointResponseReader.read(httpResponseMessage, ExpectedType.of(HttpResponseMessage.class));

		assertSame(httpResponseMessage, result);
	}

	@Test
	public void shouldReturnTheSameHttpResponseHeadersWhenReturnTypeIsHeaders() {
		Headers responseHeaders = new Headers();

		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(responseHeaders);

		Object result = endpointResponseReader.read(httpResponseMessage, ExpectedType.of(Headers.class));

		assertSame(responseHeaders, result);
	}

	@Test
	public void shouldReturnNullWhenHttpResponseCodeIsNotReadable() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(EndpointResponseCode.noContent());

		Object result = endpointResponseReader.read(httpResponseMessage, ExpectedType.of(String.class));

		assertNull(result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnParameterizedEndpointResponseObjectWhenEndpointResponseTypeIsExpected() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream(endpointResult.getBytes()));

		Type expectedType = new SimpleParameterizedType(EndpointResponse.class, null, String.class);

		EndpointResponse<String> result = (EndpointResponse<String>) endpointResponseReader.read(httpResponseMessage, ExpectedType.of(expectedType));

		assertEquals(endpointResult, result.body());
	}
}
