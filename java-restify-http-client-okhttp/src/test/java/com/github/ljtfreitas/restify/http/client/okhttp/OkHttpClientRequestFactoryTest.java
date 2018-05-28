package com.github.ljtfreitas.restify.http.client.okhttp;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.io.InputStreamContent;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;

import okhttp3.OkHttpClient;

public class OkHttpClientRequestFactoryTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MockServerClient mockServerClient;
	
	private OkHttpClientRequestFactory okHttpClientRequestFactory;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		okHttpClientRequestFactory = new OkHttpClientRequestFactory();
	}

	@Test
	public void shouldSendGetRequest() {
		String responseBody = "{\"name\": \"Tiago de Freitas Lima\",\"age\":31}";

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json(responseBody)));

		HttpResponseMessage response = okHttpClientRequestFactory.createOf(new EndpointRequest(URI.create("http://localhost:7080/json"), "GET"))
				.execute();

		assertEquals(responseBody, new InputStreamContent(response.body()).asString());
		assertEquals("application/json", response.headers().get("Content-Type").get().value());
		assertEquals(StatusCode.ok(), response.status());
	}

	@Test
	public void shouldSendGetRequestAsync() throws Exception {
		String responseBody = "{\"name\": \"Tiago de Freitas Lima\",\"age\":31}";

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json(responseBody)));

		CompletableFuture<HttpResponseMessage> future = okHttpClientRequestFactory
				.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/json"), "GET"))
					.executeAsync();

		HttpResponseMessage response = future.get();
		
		assertEquals(responseBody, new InputStreamContent(response.body()).asString());
		assertEquals("application/json", response.headers().get("Content-Type").get().value());
		assertEquals(StatusCode.ok(), response.status());
	}
	
	@Test
	public void shouldSendPostRequest() throws IOException {
		String requestBody = "{\"name\":\"Tiago de Freitas Lima\",\"age\":31}";
		String responseBody = "OK";

		HttpRequest httpRequest = request()
			.withMethod("POST")
			.withPath("/json")
			.withHeader("Content-Type", "application/json")
			.withBody(json(requestBody));

		mockServerClient
			.when(httpRequest)
			.respond(response()
				.withStatusCode(201)
				.withHeader("Content-Type", "text/plain")
				.withBody(exact(responseBody)));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/json"), "POST",
				new Headers(Header.contentType("application/json")), requestBody);
		
		OkHttpClientRequest request = okHttpClientRequestFactory.createOf(endpointRequest);
		request.body().write(requestBody.getBytes());
		request.body().flush();

		HttpResponseMessage response = request.execute();

		assertEquals(responseBody, new InputStreamContent(response.body()).asString());
		assertEquals("text/plain", response.headers().get("Content-Type").get().value());
		assertEquals(StatusCode.created(), response.status());

		mockServerClient.verify(httpRequest, once());
	}

	@Test
	public void shouldSendPostRequestAsync() throws IOException, Exception {
		String requestBody = "{\"name\":\"Tiago de Freitas Lima\",\"age\":31}";
		String responseBody = "OK";

		HttpRequest httpRequest = request()
			.withMethod("POST")
			.withPath("/json")
			.withHeader("Content-Type", "application/json")
			.withBody(json(requestBody));

		mockServerClient
			.when(httpRequest)
			.respond(response()
				.withStatusCode(201)
				.withHeader("Content-Type", "text/plain")
				.withBody(exact(responseBody)));

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/json"), "POST",
				new Headers(Header.contentType("application/json")), requestBody);
		
		OkHttpClientRequest request = okHttpClientRequestFactory.createOf(endpointRequest);
		request.body().write(requestBody.getBytes());
		request.body().flush();

		HttpResponseMessage response = request.executeAsync().get();

		assertEquals(responseBody, new InputStreamContent(response.body()).asString());
		assertEquals("text/plain", response.headers().get("Content-Type").get().value());
		assertEquals(StatusCode.created(), response.status());

		mockServerClient.verify(httpRequest, once());
	}

	@Test
	public void shouldThrowExceptionOnTimeout() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/slow"))
			.respond(response()
				.withDelay(TimeUnit.MILLISECONDS, 3000));

		expectedException.expect(isA(HttpClientException.class));
		expectedException.expectCause(isA(SocketTimeoutException.class));

		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.readTimeout(2000, TimeUnit.MILLISECONDS)
					.build();
		
		okHttpClientRequestFactory = new OkHttpClientRequestFactory(okHttpClient);

		okHttpClientRequestFactory.createOf(new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET"))
				.execute();
	}

	@Test
	public void shouldThrowExceptionOnTimeoutAsync() throws Exception {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/slow"))
			.respond(response()
				.withDelay(TimeUnit.MILLISECONDS, 3000));

		expectedException.expect(isA(ExecutionException.class));
		expectedException.expectCause(isA(HttpClientException.class));

		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.readTimeout(2000, TimeUnit.MILLISECONDS)
					.build();
		
		okHttpClientRequestFactory = new OkHttpClientRequestFactory(okHttpClient);

		CompletableFuture<HttpResponseMessage> future = okHttpClientRequestFactory
			.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET"))
				.executeAsync();

		Thread.sleep(3000);

		assertTrue(future.isCompletedExceptionally());

		future.get();
	}
}
