package com.github.ljtfreitas.restify.http.client.request.grizzly;

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
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;

public class GrizzlyHttpClientRequestFactoryTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MockServerClient mockServerClient;

	private GrizzlyHttpClientRequestFactory subject;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		subject = new GrizzlyHttpClientRequestFactory();
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

		CompletableFuture<HttpClientResponse> responseAsFuture = subject
				.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/json"), "GET"))
					.executeAsync()
						.toCompletableFuture();

		HttpResponseMessage response = responseAsFuture.get();

		assertEquals(responseBody, new InputStreamContent(response.body().input()).asString());
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

		AsyncHttpClientRequest request = subject.createAsyncOf(endpointRequest);
		request.body().output().write(requestBody.getBytes());
		request.body().output().flush();

		HttpResponseMessage response = request.executeAsync().toCompletableFuture().get();

		assertEquals(responseBody, new InputStreamContent(response.body().input()).asString());
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

		AsyncHttpClientConfig configuration = new AsyncHttpClientConfig.Builder()
				.setRequestTimeout(2000)
					.build();

		subject = new GrizzlyHttpClientRequestFactory(configuration);

		expectedException.expect(ExecutionException.class);
		expectedException.expectCause(isA(HttpClientException.class));
		expectedException.expect(deepCauseIsA(TimeoutException.class));

		CompletableFuture<HttpClientResponse> future = subject
			.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET"))
				.executeAsync()
					.toCompletableFuture();

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
		expectedException.expect(deepCauseIsA(TimeoutException.class));

		EndpointRequest request = new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET", new Headers(), null, void.class, null,
				new EndpointRequestMetadata(Arrays.asList(timeout)));

		CompletableFuture<HttpClientResponse> future = subject
			.createAsyncOf(request)
				.executeAsync()
					.toCompletableFuture();

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
		Realm realm = new Realm.RealmBuilder()
				.setPrincipal("user")
				.setPassword("password")
				.setScheme(AuthScheme.BASIC)
				.setUsePreemptiveAuth(true)
				.build();

		AsyncHttpClientConfig configuration = new AsyncHttpClientConfig.Builder()
				.setRealm(realm)
				.build();

		subject = new GrizzlyHttpClientRequestFactory(configuration);

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

		HttpResponseMessage response = subject
				.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/authenticated"), "GET"))
					.executeAsync()
						.toCompletableFuture()
							.get();

		assertEquals("ok", new InputStreamContent(response.body().input()).asString());

		mockServerClient.verify(authenticatedRequest);
	}
}
