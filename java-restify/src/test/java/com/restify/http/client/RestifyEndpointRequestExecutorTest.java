package com.restify.http.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.interceptor.EndpointRequestInterceptorStack;

@RunWith(MockitoJUnitRunner.class)
public class RestifyEndpointRequestExecutorTest {

	@Mock
	private HttpClientRequestFactory httpClientRequestFactoryMock;

	@Mock
	private EndpointRequestInterceptorStack endpointRequestInterceptorStackMock;

	@Mock
	private EndpointRequestWriter endpointRequestWriterMock;

	@Mock
	private EndpointResponseReader endpointResponseReaderMock;

	@InjectMocks
	private RestifyEndpointRequestExecutor endpointRequestExecutor;

	private String endpointResult;

	private EndpointResponse response;

	@Before
	public void setup() {
		endpointResult = "endpoint request result";

		response = new SimpleEndpointResponse(new EndpointResponseCode(200),
				new ByteArrayInputStream(endpointResult.getBytes()));

		when(endpointRequestInterceptorStackMock.apply(any())).then(returnsFirstArg());

		when(endpointResponseReaderMock.read(response, String.class))
			.thenReturn(endpointResult);
	}

	@Test
	public void shouldExecuteHttpClientRequestWithoutBody() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET", String.class);

		when(httpClientRequestFactoryMock.createOf(endpointRequest))
			.thenReturn(new SimpleHttpClientRequest(response));

		Object result = endpointRequestExecutor.execute(endpointRequest);

		assertEquals(endpointResult, result);

		verify(endpointRequestWriterMock, never()).write(any(), any());
		verify(endpointResponseReaderMock).read(response, String.class);
	}

	@Test
	public void shouldExecuteHttpClientRequestWithBody() throws Exception {
		String body = "endpoint request body";

		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST", new Headers(),
				body, String.class);

		SimpleHttpClientRequest request = new SimpleHttpClientRequest(response);

		when(httpClientRequestFactoryMock.createOf(endpointRequest))
			.thenReturn(request);

		Object result = endpointRequestExecutor.execute(endpointRequest);

		assertEquals(endpointResult, result);

		verify(endpointRequestWriterMock).write(endpointRequest, request);
		verify(endpointResponseReaderMock).read(response, String.class);
	}

	private class SimpleHttpClientRequest implements HttpClientRequest {

		private final EndpointResponse response;

		private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		private final Headers headers = new Headers();

		public SimpleHttpClientRequest(EndpointResponse response) {
			this.response = response;
		}

		@Override
		public OutputStream output() {
			return outputStream;
		}

		@Override
		public String charset() {
			return "UTF-8";
		}

		@Override
		public EndpointResponse execute() throws RestifyHttpException {
			return response;
		}

		@Override
		public Headers headers() {
			return headers;
		}
	}

	private class SimpleEndpointResponse extends EndpointResponse {

		private final InputStream stream;

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
