package com.github.ljtfreitas.restify.http.client.request.vertx;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.io.InputStreamContent;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestMetadata;
import com.github.ljtfreitas.restify.http.client.request.Timeout;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;

import io.vertx.core.VertxException;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClientOptions;

@RunWith(VertxUnitRunner.class)
public class VertxHttpClientRequestFactoryTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	private MockServerClient mockServerClient;

	private VertxHttpClientRequestFactory subject;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		subject = new VertxHttpClientRequestFactory();
	}

	@Test
	public void shouldSendGetRequest(TestContext testContext) throws Exception {
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

		Async async = testContext.async();

		responseAsFuture.whenComplete((response, failure) -> {
			if (failure != null) {
				testContext.fail(failure);

			} else {
				testContext.verify(s -> {
					assertThat(new InputStreamContent(response.body().input()).asString(), equalTo(responseBody));
					assertThat(response.headers().get("Content-Type").get().value(), equalTo("application/json"));
					assertThat(response.status(), Matchers.is(StatusCode.ok()));

					async.complete();
				});
			}
		});

		async.await();
	}

	@Test
	public void shouldSendPostRequest(TestContext testContext) throws IOException, Exception {
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

		Async async = testContext.async();

		request.executeAsync().whenComplete((response, failure) -> {
			if (failure != null) {
				testContext.fail(failure);

			} else {
				testContext.verify(s -> {
					assertThat(new InputStreamContent(response.body().input()).asString(), equalTo(responseBody));
					assertThat(response.headers().get("Content-Type").get().value(), equalTo("text/plain"));
					assertThat(response.status(), is(StatusCode.created()));

					async.complete();
				});
			}
		});

		async.await();
	}

	@Test
	public void shouldThrowExceptionOnTimeout(TestContext testContext) throws Exception {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/slow"))
			.respond(response()
				.withDelay(TimeUnit.MILLISECONDS, 3000));

		WebClientOptions webClientOptions = new WebClientOptions()
			.setIdleTimeout(2000)
			.setIdleTimeoutUnit(TimeUnit.MILLISECONDS);

		subject = new VertxHttpClientRequestFactory(webClientOptions);

		AsyncHttpClientRequest request = subject.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET"));

		Async async = testContext.async();

		request.executeAsync().whenComplete((response, failure) -> {
			if (response != null) {
				testContext.fail("A timeout exception was expected.");

			} else {
				testContext.verify(s -> {
					assertThat(failure, instanceOf(HttpClientException.class));
					assertThat(failure.getCause(), instanceOf(VertxException.class));

					async.complete();
				});
			}
		});

		async.await();
	}

	@Test
	public void shouldThrowExceptionOnTimeoutUsingAnnotation(TestContext testContext) throws Exception {
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

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET", new Headers(), null, void.class, null,
				new EndpointRequestMetadata(Arrays.asList(timeout)));

		AsyncHttpClientRequest request = subject.createAsyncOf(endpointRequest);

		Async async = testContext.async();

		request.executeAsync().whenComplete((response, failure) -> {
			if (response != null) {
				testContext.fail("A timeout exception was expected.");

			} else {
				testContext.verify(s -> {
					assertThat(failure, instanceOf(HttpClientException.class));
					assertThat(failure.getCause(), instanceOf(TimeoutException.class));

					async.complete();
				});
			}
		});

		async.await();
	}
}
