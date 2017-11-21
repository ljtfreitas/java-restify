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

import com.github.ljtfreitas.restify.http.client.message.ContentType;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReader;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.reflection.JavaType;

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

	@Mock
	private HttpRequestMessage httpRequestMessage;

	@Mock
	private HttpResponseMessage httpResponseMessage;

	private String endpointResult;
	
	@Before
	public void setup() {
		endpointResult = "expected result";
		
		when(httpResponseMessage.body()).thenReturn(new ByteArrayInputStream(endpointResult.getBytes()));
		when(httpResponseMessage.headers()).thenReturn(new Headers(Header.contentType("text/plain")));
		when(httpResponseMessage.status()).thenReturn(StatusCode.ok());
		when(httpResponseMessage.readable()).thenReturn(true);

		when(httpMessageConvertersMock.readerOf(any(), any()))
			.thenReturn(Optional.empty());

		when(httpMessageConvertersMock.readerOf(ContentType.of("text/plain"), String.class))
				.thenReturn(Optional.of(httpMessageReaderMock));

		when(httpMessageReaderMock.read(any(), eq(String.class))).thenReturn(endpointResult);
	}

	@Test
	public void shouldReadSuccessResponse() {
		EndpointResponse<String> response = endpointResponseReader.read(httpResponseMessage, JavaType.of(String.class));

		assertEquals(endpointResult, response.body());
	}

	@Test(expected = HttpMessageReadException.class)
	public void shouldThrowExceptionWhenTheContentTypeCantBeRead() {
		when(httpResponseMessage.headers()).thenReturn(new Headers(Header.contentType("application/json")));

		endpointResponseReader.read(httpResponseMessage, JavaType.of(String.class));
	}

	@Test
	public void shouldCallEndpointResponseErrorFallbackWhenResponseStatusCodeIsError() {
		when(httpResponseMessage.status()).thenReturn(StatusCode.internalServerError());

		JavaType responseType = JavaType.of(String.class);

		endpointResponseReader.read(httpResponseMessage, responseType);

		verify(endpointResponseErrorFallbackMock)
			.onError(httpResponseMessage, responseType);
	}

	@Test
	public void shouldReturnEmptyResponseWhenJavaTypeIsVoid() {
		EndpointResponse<Void> response = endpointResponseReader.read(httpResponseMessage, JavaType.of(Void.class));

		assertNull(response.body());
	}

	@Test
	public void shouldReturnEmptyResponseWhenHttpResponseCodeIsNotReadable() {
		when(httpResponseMessage.readable()).thenReturn(false);

		EndpointResponse<String> response = endpointResponseReader.read(httpResponseMessage, JavaType.of(String.class));

		assertNull(response.body());
	}
}
