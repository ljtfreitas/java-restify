package com.github.ljtfreitas.restify.http.client.apache.httpclient;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
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
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestMetadata;
import com.github.ljtfreitas.restify.http.client.request.Timeout;

public class ApacheAsyncHttpClientRequestFactoryTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MockServerClient mockServerClient;

	private ApacheAsyncHttpClientRequestFactory apacheAsyncHttpClientRequestFactory;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		apacheAsyncHttpClientRequestFactory = new ApacheAsyncHttpClientRequestFactory();
	}

	@Test
	public void shouldSendGetRequest() throws Exception {
		String responseBody = "{\"name\": \"Tiago de Freitas Lima\",\"age\":31}";

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json(responseBody)));

		CompletableFuture<HttpResponseMessage> responseAsFuture = apacheAsyncHttpClientRequestFactory
				.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/json"), "GET"))
					.executeAsync();

		HttpResponseMessage response = responseAsFuture.get();

		assertEquals(responseBody, new InputStreamContent(response.body()).asString());
		assertEquals("application/json", response.headers().get("Content-Type").get().value());
		assertEquals(StatusCode.ok(), response.status());
	}

	@Test
	public void shouldSendPostRequest() throws IOException, Exception {
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

		ApacheAsyncHttpClientRequest request = apacheAsyncHttpClientRequestFactory.createAsyncOf(endpointRequest);
		request.output().write(requestBody.getBytes());
		request.output().flush();

		HttpResponseMessage response = request.executeAsync().get();

		assertEquals(responseBody, new InputStreamContent(response.body()).asString());
		assertEquals("text/plain", response.headers().get("Content-Type").get().value());
		assertEquals(StatusCode.created(), response.status());

		mockServerClient.verify(httpRequest, once());
	}

	@Test
	public void shouldThrowExceptionOnTimeout() throws Exception {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/slow"))
			.respond(response()
				.withDelay(TimeUnit.MILLISECONDS, 3000));

		RequestConfig configuration = RequestConfig.custom()
				.setSocketTimeout(2000)
					.build();

		apacheAsyncHttpClientRequestFactory = new ApacheAsyncHttpClientRequestFactory(configuration);

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpClientException.class));
		expectedException.expect(deepCauseIsA(SocketTimeoutException.class));

		CompletableFuture<HttpResponseMessage> future = apacheAsyncHttpClientRequestFactory
			.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET"))
				.executeAsync();

		Thread.sleep(3000);

		assertTrue(future.isCompletedExceptionally());

		future.get();
	}

	@Test
	public void shouldThrowExceptionOnTimeoutUsingAnnotation() throws Exception {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/slow"))
			.respond(response()
				.withDelay(TimeUnit.MILLISECONDS, 3000));

		Timeout timeout = new Timeout() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Timeout.class;
			}

			@Override
			public long read() {
				return 2000;
			}

			@Override
			public long connection() {
				return 2000;
			}
		};

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpClientException.class));
		expectedException.expect(deepCauseIsA(SocketTimeoutException.class));

		EndpointRequest request = new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET", new Headers(), null, void.class, null,
				new EndpointRequestMetadata(Arrays.asList(timeout)));

		CompletableFuture<HttpResponseMessage> future = apacheAsyncHttpClientRequestFactory
			.createAsyncOf(request)
				.executeAsync();

		Thread.sleep(3000);

		assertTrue(future.isCompletedExceptionally());

		future.get();
	}

	private Matcher<Exception> deepCauseIsA(Class<? extends Exception> type) {
		return new CustomTypeSafeMatcher<Exception>("The root cause should be..." + type) {

			@Override
			protected boolean matchesSafely(Exception e) {
				Throwable deepCause = deepCauseOf(e);
				return type.isAssignableFrom(deepCause.getClass());
			}

			private Throwable deepCauseOf(Throwable e) {
				return e.getCause() == null ? e : deepCauseOf(e.getCause());
			}
		};
	}

	@Test
	public void shouldExecuteAuthenticatedRequest() throws Exception {
		CredentialsProvider provider = new BasicCredentialsProvider();

		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("user", "password");
		provider.setCredentials(AuthScope.ANY, credentials);

		AuthCache authCache = new BasicAuthCache();
		authCache.put(new HttpHost("localhost", 7080), new BasicScheme());

		HttpClientContext context = new HttpClientContext();
		context.setCredentialsProvider(provider);
		context.setAuthCache(authCache);

		apacheAsyncHttpClientRequestFactory = new ApacheAsyncHttpClientRequestFactory(context);

		HttpRequest authenticatedRequest = request()
				.withMethod("GET")
				.withPath("/authenticated")
				.withHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==");

		mockServerClient
			.when(authenticatedRequest)
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "text/plain")
					.withBody(exact("ok")));

		HttpResponseMessage response = apacheAsyncHttpClientRequestFactory
				.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/authenticated"), "GET"))
					.executeAsync()
						.get();

		assertEquals("ok", new InputStreamContent(response.body()).asString());

		mockServerClient.verify(authenticatedRequest);
	}
}
