package com.restify.http.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.converter.HttpMessageWriter;
import com.restify.http.client.converter.SimpleHttpRequestMessage;
import com.restify.http.contract.ContentType;

@RunWith(MockitoJUnitRunner.class)
public class EndpointRequestWriterTest {

	@Mock
	private HttpMessageConverters httpMessageConvertersMock;

	@Mock
	private HttpMessageWriter<Object> httpMessageWriterMock;

	@InjectMocks
	private EndpointRequestWriter endpointRequestWriter;

	@Before
	public void setup() {
		when(httpMessageConvertersMock.writerOf(ContentType.of("text/plain"), String.class))
				.thenReturn(Optional.of(httpMessageWriterMock));
	}

	@Test
	public void shouldExecuteHttpClientRequestWithBody() throws Exception {
		String body = "body";

		Headers headers = new Headers(new Header("Content-Type", "text/plain"));

		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST", headers, body,
				String.class);

		SimpleHttpRequestMessage httpRequestMessage = new SimpleHttpRequestMessage(headers);

		endpointRequestWriter.write(endpointRequest, httpRequestMessage);

		verify(httpMessageConvertersMock).writerOf(ContentType.of("text/plain"), String.class);
		verify(httpMessageWriterMock).write(body, httpRequestMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenHttpRequestMessageHasNoBody() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST");

		endpointRequestWriter.write(endpointRequest, new SimpleHttpRequestMessage());
	}

	@Test
	public void shouldAppendCharsetParameterOnContentTypeHeader() throws Exception {
		String body = "body";

		Headers headers = new Headers(new Header("Content-Type", "text/plain"));

		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST", headers, body,
				String.class);

		SimpleHttpRequestMessage httpRequestMessage = new SimpleHttpRequestMessage(headers);

		endpointRequestWriter.write(endpointRequest, httpRequestMessage);

		assertEquals("text/plain; charset=UTF-8", headers.get(Headers.CONTENT_TYPE).get().value());
	}
}
