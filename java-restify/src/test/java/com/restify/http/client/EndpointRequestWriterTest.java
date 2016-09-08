package com.restify.http.client;

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

import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.converter.SimpleHttpRequestMessage;

@RunWith(MockitoJUnitRunner.class)
public class EndpointRequestWriterTest {

	@Mock
	private HttpMessageConverters httpMessageConvertersMock;

	@Mock
	private HttpMessageConverter<Object> httpMessageConverterMock;

	@InjectMocks
	private EndpointRequestWriter endpointRequestWriter;

	private SimpleHttpRequestMessage httpRequestMessage;

	@Before
	public void setup() {
		when(httpMessageConvertersMock.writerOf("text/plain", String.class))
				.thenReturn(Optional.of(httpMessageConverterMock));

		httpRequestMessage = new SimpleHttpRequestMessage();
	}

	@Test
	public void shouldExecuteHttpClientRequestWithBody() throws Exception {
		String body = "body";

		Headers headers = new Headers(new Header("Content-Type", "text/plain"));

		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST", headers, body,
				String.class);

		endpointRequestWriter.write(endpointRequest, httpRequestMessage);

		verify(httpMessageConvertersMock).writerOf("text/plain", String.class);
		verify(httpMessageConverterMock).write(body, httpRequestMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenHttpRequestMessageHasNoBody() throws Exception {

		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST");

		endpointRequestWriter.write(endpointRequest, httpRequestMessage);
	}
}
