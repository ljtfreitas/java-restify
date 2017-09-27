package com.github.ljtfreitas.restify.http.client.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageReader;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.SimpleHttpRequestMessage;
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

	private HttpRequestMessage httpRequestMessage;

	private String endpointResult;

	private Headers headers;

	@Before
	public void setup() {
		httpRequestMessage = new SimpleHttpRequestMessage(new EndpointRequest(URI.create("http://any.api"), "GET"));

		endpointResult = "expected result";

		when(httpMessageConvertersMock.readerOf(any(), any()))
			.thenReturn(Optional.empty());

		when(httpMessageConvertersMock.readerOf(ContentType.of("text/plain"), String.class))
				.thenReturn(Optional.of(httpMessageReaderMock));

		when(httpMessageReaderMock.read(any(), eq(String.class))).thenReturn(endpointResult);

		headers = new Headers(Header.contentType("text/plain"));
	}

	@Test
	public void shouldReadSuccessResponse() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream(endpointResult.getBytes()),
				httpRequestMessage, headers);

		EndpointResponse<String> response = endpointResponseReader.read(httpResponseMessage, JavaType.of(String.class));

		assertEquals(endpointResult, response.body());
	}

	@Test(expected = RestifyHttpMessageReadException.class)
	public void shouldThrowExceptionWhenTheContentTypeCantBeRead() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(new ByteArrayInputStream(endpointResult.getBytes()),
				httpRequestMessage, new Headers(Header.contentType("application/json")));

		endpointResponseReader.read(httpResponseMessage, JavaType.of(String.class));
	}

	@Test
	public void shouldCallEndpointResponseErrorFallbackWhenResponseStatusCodeIsError() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(StatusCode.internalServerError(), httpRequestMessage);

		JavaType responseType = JavaType.of(String.class);

		endpointResponseReader.read(httpResponseMessage, responseType);

		verify(endpointResponseErrorFallbackMock)
			.onError(httpResponseMessage, responseType);
	}

	@Test
	public void shouldReturnEmptyResponseWhenJavaTypeIsVoid() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage();

		EndpointResponse<Void> response = endpointResponseReader.read(httpResponseMessage, JavaType.of(Void.class));

		assertNull(response.body());
	}

	@Test
	public void shouldReturnEmptyResponseWhenHttpResponseCodeIsNotReadable() {
		HttpResponseMessage httpResponseMessage = new SimpleHttpResponseMessage(StatusCode.noContent(), httpRequestMessage);

		EndpointResponse<String> response = endpointResponseReader.read(httpResponseMessage, JavaType.of(String.class));

		assertNull(response.body());
	}
}
