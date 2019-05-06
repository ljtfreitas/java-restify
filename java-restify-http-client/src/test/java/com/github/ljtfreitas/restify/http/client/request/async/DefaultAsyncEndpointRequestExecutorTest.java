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
import java.util.concurrent.CompletionStage;
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
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageException;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;
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
	private AsyncHttpClientRequest asyncHttpClientRequest;

	@Mock
	private HttpClientResponse httpClientResponse;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private DefaultAsyncEndpointRequestExecutor subject;

	@Before
	public void before() {
		Executor executor = r -> r.run();

		subject = new DefaultAsyncEndpointRequestExecutor(executor, asyncHttpClientRequestFactory, writer, reader);

		when(asyncHttpClientRequestFactory.createAsyncOf(notNull(EndpointRequest.class)))
			.thenReturn(asyncHttpClientRequest);

		when(asyncHttpClientRequest.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(httpClientResponse));
	}

	@Test
	public void shouldExecuteHttpClientRequestWithoutBody() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET", String.class);

		EndpointResponse<Object> endpointResponse = EndpointResponse.of(StatusCode.ok(), "hello", new Headers());

		when(reader.read(httpClientResponse, endpointRequest.responseType()))
			.thenReturn(endpointResponse);

		CompletionStage<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		assertEquals(endpointResponse, result.toCompletableFuture().get());

		verify(writer, never()).write(any(), any());
		verify(reader).read(httpClientResponse, JavaType.of(String.class));
	}

	@Test
	public void shouldExecuteHttpClientRequestWithBody() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "POST", new Headers(),
				"endpoint request body", String.class);

		EndpointResponse<Object> endpointResponse = EndpointResponse.of(StatusCode.ok(), "hello", new Headers());

		when(reader.read(httpClientResponse, endpointRequest.responseType()))
			.thenReturn(endpointResponse);

		CompletionStage<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		assertEquals(endpointResponse, result.toCompletableFuture().get());

		verify(writer).write(endpointRequest, asyncHttpClientRequest);
		verify(reader).read(httpClientResponse, JavaType.of(String.class));
	}

	@Test
	public void shouldThrowExceptionWhenRequestThrowHttpClientError() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET");

		CompletableFuture<HttpClientResponse> future = new CompletableFuture<>();
		future.completeExceptionally(new HttpClientException("http client error"));

		when(asyncHttpClientRequest.executeAsync())
			.thenReturn(future);

		CompletionStage<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpClientException.class));

		result.toCompletableFuture().get();
	}

	@Test
	public void shouldThrowExceptionWhenRequestThrowHttpMessageError() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET");

		CompletableFuture<HttpClientResponse> future = new CompletableFuture<>();
		future.completeExceptionally(new HttpMessageException("http message error"));

		when(asyncHttpClientRequest.executeAsync())
			.thenReturn(future);

		CompletionStage<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpMessageException.class));

		result.toCompletableFuture().get();
	}

	@Test
	public void shouldThrowExceptionWhenRequestThrowIOError() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET");

		CompletableFuture<HttpClientResponse> future = new CompletableFuture<>();
		future.completeExceptionally(new IOException("http message error"));

		when(asyncHttpClientRequest.executeAsync())
			.thenReturn(future);

		CompletionStage<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpClientException.class));

		result.toCompletableFuture().get();
	}

	@Test
	public void shouldThrowExceptionWhenRequestThrowUnknownError() throws Exception {
		EndpointRequest endpointRequest = new EndpointRequest(new URI("http://my.api.com/path"), "GET");

		CompletableFuture<HttpClientResponse> future = new CompletableFuture<>();
		future.completeExceptionally(new RuntimeException("whatever"));

		when(asyncHttpClientRequest.executeAsync())
			.thenReturn(future);

		CompletionStage<EndpointResponse<Object>> result = subject.executeAsync(endpointRequest);

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpClientException.class));

		result.toCompletableFuture().get();
	}
}
