package com.github.ljtfreitas.restify.http.client.request;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEndpointRequestExecutorTest {

	@Mock
	private HttpClientRequestFactory httpClientRequestFactoryMock;

	@Mock
	private EndpointRequestWriter endpointRequestWriterMock;

	@Mock
	private EndpointResponseReader endpointResponseReaderMock;

	@InjectMocks
	private DefaultEndpointRequestExecutor endpointRequestExecutor;

	private SimpleEndpointResponse<String> endpointResult;

	@Mock
	private HttpResponseMessage response;

	@Before
	public void setup() {
		endpointResult = new SimpleEndpointResponse<>("endpoint request result");

		when(response.body()).thenReturn(new ByteArrayInputStream(endpointResult.body().getBytes()));

		when(endpointResponseReaderMock.<String> read(response, JavaType.of(String.class)))
			.thenReturn(endpointResult);
	}

	@Test
	public void shouldExecuteHttpClientRequestWithoutBody() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET", String.class);

		when(httpClientRequestFactoryMock.createOf(endpointRequest))
			.thenReturn(new SimpleHttpClientRequest(endpointRequest, response));

		Object result = endpointRequestExecutor.execute(endpointRequest);

		assertEquals(endpointResult, result);

		verify(endpointRequestWriterMock, never()).write(any(), any());
		verify(endpointResponseReaderMock).read(response, JavaType.of(String.class));
	}

	@Test
	public void shouldExecuteHttpClientRequestWithBody() throws Exception {
		String body = "endpoint request body";

		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST", new Headers(),
				body, String.class);

		SimpleHttpClientRequest request = new SimpleHttpClientRequest(endpointRequest, response);

		when(httpClientRequestFactoryMock.createOf(endpointRequest))
			.thenReturn(request);

		Object result = endpointRequestExecutor.execute(endpointRequest);

		assertEquals(endpointResult, result);

		verify(endpointRequestWriterMock).write(endpointRequest, request);
		verify(endpointResponseReaderMock).read(response, JavaType.of(String.class));
	}

	private class SimpleHttpClientRequest implements HttpClientRequest {

		private final EndpointRequest source;
		private final HttpResponseMessage response;

		private final RequestBody body = new RequestBody();

		public SimpleHttpClientRequest(EndpointRequest source, HttpResponseMessage response) {
			this.source = source;
			this.response = response;
		}

		@Override
		public URI uri() {
			return source.endpoint();
		}

		@Override
		public String method() {
			return source.method();
		}

		@Override
		public RequestBody body() {
			return body;
		}

		@Override
		public Charset charset() {
			return Encoding.UTF_8.charset();
		}

		@Override
		public HttpResponseMessage execute() throws HttpClientException {
			return response;
		}

		@Override
		public Headers headers() {
			return source.headers();
		}

		@Override
		public HttpRequestMessage replace(Header header) {
			throw new UnsupportedOperationException();
		}
	}

	private class SimpleEndpointResponse<T> extends EndpointResponse<T> {
		public SimpleEndpointResponse(T body) {
			super(null, null, body);
		}
	}
}
