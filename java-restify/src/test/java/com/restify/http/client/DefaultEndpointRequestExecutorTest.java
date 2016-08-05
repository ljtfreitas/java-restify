package com.restify.http.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.interceptor.EndpointRequestInterceptorStack;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEndpointRequestExecutorTest {

	@Mock
	private HttpMessageConverters httpMessageConvertersMock;

	@Mock
	private HttpClientRequestFactory httpClientRequestFactoryMock;

	@Mock
	private EndpointRequestInterceptorStack endpointRequestInterceptorStackMock;

	@InjectMocks
	private DefaultEndpointRequestExecutor endpointRequestExecutor;

	@Mock
	private HttpMessageConverter<Object> jsonHttpMessageConverterMock;

	@Mock
	private HttpMessageConverter<Object> textPlainHttpMessageConverterMock;

	private String endpointResult;

	@Before
	public void setup() {
		when(httpMessageConvertersMock.writerOf(eq("application/json"), any()))
			.thenReturn(Optional.of(jsonHttpMessageConverterMock));

		when(httpMessageConvertersMock.readerOf(eq("application/json"), any()))
			.thenReturn(Optional.of(jsonHttpMessageConverterMock));

		when(httpMessageConvertersMock.writerOf(eq("text/plain"), any()))
			.thenReturn(Optional.of(textPlainHttpMessageConverterMock));

		when(httpMessageConvertersMock.readerOf(eq("text/plain"), any()))
			.thenReturn(Optional.of(textPlainHttpMessageConverterMock));

		endpointResult = "endpoint request result";

		when(jsonHttpMessageConverterMock.read(eq(String.class), notNull(HttpResponseMessage.class)))
			.thenReturn(endpointResult);

		when(textPlainHttpMessageConverterMock.read(eq(String.class), notNull(HttpResponseMessage.class)))
			.thenReturn(endpointResult);

		when(endpointRequestInterceptorStackMock.apply(any()))
			.then(returnsFirstArg());
	}

	@Test
	public void shouldExecuteHttpClientRequestWithoutBody() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET", String.class);

		SimpleEndpointResponse response = new SimpleEndpointResponse(new EndpointResponseCode(200),
				new ByteArrayInputStream(endpointResult.getBytes()));

		when(httpClientRequestFactoryMock.createOf(endpointRequest))
			.thenReturn(new SimpleHttpClientRequest(response));

		Object result = endpointRequestExecutor.execute(endpointRequest);

		assertEquals("endpoint request result", result);

		verify(textPlainHttpMessageConverterMock).read(String.class, response);
	}

	@Test
	public void shouldExecuteHttpClientRequestWithBody() throws Exception {
		MyModel body = new MyModel("My Name", 31);

		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST",
				new Headers(new Header("Content-Type", "application/json")), body, String.class);

		SimpleEndpointResponse response = new SimpleEndpointResponse(new EndpointResponseCode(200),
				new Headers(new Header("Content-Type", "application/json")), new ByteArrayInputStream("endpoint request result".getBytes()));

		SimpleHttpClientRequest request = new SimpleHttpClientRequest(response);

		when(httpClientRequestFactoryMock.createOf(endpointRequest))
			.thenReturn(request);

		Object result = endpointRequestExecutor.execute(endpointRequest);

		assertEquals("endpoint request result", result);

		verify(jsonHttpMessageConverterMock).write(body, request);
		verify(jsonHttpMessageConverterMock).read(String.class, response);
	}

	private class SimpleHttpClientRequest implements HttpClientRequest {

		private final EndpointResponse response;

		private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

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

	private class MyModel {

		@SuppressWarnings("unused")
		final String name;

		@SuppressWarnings("unused")
		final int age;

		MyModel(String name, int age) {
			this.name = name;
			this.age = age;
		}


	}
}
