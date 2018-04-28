package com.github.ljtfreitas.restify.http.client.request.async;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageException;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAsyncEndpointRequestExecutorTest {

	@Mock
	private AsyncHttpClientRequestFactory asyncHttpClientRequestFactory;

	@Mock
	private EndpointRequestWriter writer;

	@Mock
	private EndpointResponseReader reader;

	@Mock
	private EndpointRequestExecutor endpointRequestExecutor;

	@Mock
	private AsyncHttpClientRequest asyncHttpClientRequest;

	@Mock
	private HttpResponseMessage httpResponseMessage;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private DefaultAsyncEndpointRequestExecutor subject;

	@Before
	public void before() {
		Executor executor = r -> r.run();

		subject = new DefaultAsyncEndpointRequestExecutor(executor, asyncHttpClientRequestFactory, writer, reader, endpointRequestExecutor);

		when(asyncHttpClientRequestFactory.createAsyncOf(notNull(EndpointRequest.class)))
			.thenReturn(asyncHttpClientRequest);

		when(asyncHttpClientRequest.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(httpResponseMessage));
	}

	@Test
	public void shouldExecuteHttpClientRequestWithoutBody() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET", String.class);

		EndpointResponse<Object> endpointResponse = new EndpointResponse<>(StatusCode.ok(), new Headers(), "hello");

		when(reader.read(httpResponseMessage, endpointRequest.responseType()))
			.thenReturn(endpointResponse);

		CompletableFuture<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		assertEquals(endpointResponse, result.get());

		verify(writer, never()).write(any(), any());
		verify(reader).read(httpResponseMessage, JavaType.of(String.class));
	}

	@Test
	public void shouldExecuteHttpClientRequestWithBody() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST", new Headers(),
				"endpoint request body", String.class);

		EndpointResponse<Object> endpointResponse = new EndpointResponse<>(StatusCode.ok(), new Headers(), "hello");

		when(reader.read(httpResponseMessage, endpointRequest.responseType()))
			.thenReturn(endpointResponse);

		CompletableFuture<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		assertEquals(endpointResponse, result.get());

		verify(writer).write(endpointRequest, asyncHttpClientRequest);
		verify(reader).read(httpResponseMessage, JavaType.of(String.class));
	}

	@Test
	public void shouldThrowExceptionWhenRequestThrowHttpClientError() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET");

		CompletableFuture<HttpResponseMessage> future = new CompletableFuture<>();
		future.completeExceptionally(new HttpClientException("http client error"));

		when(asyncHttpClientRequest.executeAsync())
			.thenReturn(future);

		CompletableFuture<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpClientException.class));

		result.get();
	}

	@Test
	public void shouldThrowExceptionWhenRequestThrowHttpMessageError() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET");

		CompletableFuture<HttpResponseMessage> future = new CompletableFuture<>();
		future.completeExceptionally(new HttpMessageException("http message error"));

		when(asyncHttpClientRequest.executeAsync())
			.thenReturn(future);

		CompletableFuture<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpMessageException.class));

		result.get();
	}

	@Test
	public void shouldThrowExceptionWhenRequestThrowIOError() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET");

		CompletableFuture<HttpResponseMessage> future = new CompletableFuture<>();
		future.completeExceptionally(new IOException("http message error"));

		when(asyncHttpClientRequest.executeAsync())
			.thenReturn(future);

		CompletableFuture<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpClientException.class));

		result.get();
	}

	@Test
	public void shouldThrowExceptionWhenRequestThrowUnknownError() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET");

		CompletableFuture<HttpResponseMessage> future = new CompletableFuture<>();
		future.completeExceptionally(new RuntimeException("whatever"));

		when(asyncHttpClientRequest.executeAsync())
			.thenReturn(future);

		CompletableFuture<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpException.class));

		result.get();
	}
}
