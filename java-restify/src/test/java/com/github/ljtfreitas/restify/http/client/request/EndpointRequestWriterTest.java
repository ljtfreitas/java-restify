package com.github.ljtfreitas.restify.http.client.request;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageWriter;
import com.github.ljtfreitas.restify.http.contract.ContentType;

@RunWith(MockitoJUnitRunner.class)
public class EndpointRequestWriterTest {

	@Mock
	private HttpMessageConverters httpMessageConvertersMock;

	@Mock
	private HttpMessageWriter<Object> httpMessageWriterMock;

	@InjectMocks
	private EndpointRequestWriter endpointRequestWriter;

	@Captor
	private ArgumentCaptor<HttpRequestMessage> httpRequestMessageCaptor;

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

		SimpleHttpRequestMessage httpRequestMessage = new SimpleHttpRequestMessage(endpointRequest);

		endpointRequestWriter.write(endpointRequest, httpRequestMessage);

		verify(httpMessageConvertersMock).writerOf(ContentType.of("text/plain"), String.class);
		verify(httpMessageWriterMock).write(eq(body), notNull(HttpRequestMessage.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenHttpRequestMessageHasNoBody() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST");

		endpointRequestWriter.write(endpointRequest, new SimpleHttpRequestMessage(endpointRequest));
	}
}
