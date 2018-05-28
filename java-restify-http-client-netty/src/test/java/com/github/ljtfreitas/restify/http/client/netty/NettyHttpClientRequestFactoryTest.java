package com.github.ljtfreitas.restify.http.client.netty;

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
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.socket.SSLFactory;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.io.InputStreamContent;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestMetadata;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.Timeout;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutException;

public class NettyHttpClientRequestFactoryTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080, 7084);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MockServerClient mockServerClient;

	private NettyHttpClientRequestFactory nettyHttpClientRequestFactory;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		nettyHttpClientRequestFactory = new NettyHttpClientRequestFactory();
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

		HttpResponseMessage response = nettyHttpClientRequestFactory.createOf(new EndpointRequest(URI.create("http://localhost:7080/json"), "GET"))
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

		CompletableFuture<HttpResponseMessage> responseAsFuture = nettyHttpClientRequestFactory
				.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/json"), "GET"))
					.executeAsync();

		responseAsFuture.thenAccept(response -> {
			assertEquals(responseBody, new InputStreamContent(response.body()).asString());
			assertEquals("application/json", response.headers().get("Content-Type").get().value());
			assertEquals(StatusCode.ok(), response.status());
		});
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

		HttpClientRequest request = nettyHttpClientRequestFactory.createOf(endpointRequest);
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

		NettyHttpClientRequest request = nettyHttpClientRequestFactory.createAsyncOf(endpointRequest);
		request.body().write(requestBody.getBytes());
		request.body().flush();

		CompletableFuture<HttpResponseMessage> responseAsFuture = request.executeAsync();

		responseAsFuture.thenAccept(response -> {
			assertEquals(responseBody, new InputStreamContent(response.body()).asString());
			assertEquals("text/plain", response.headers().get("Content-Type").get().value());
			assertEquals(StatusCode.created(), response.status());

			mockServerClient.verify(httpRequest, once());
		});
	}

	@Test
	public void shouldThrowExceptionOnTimeout() {
		NettyHttpClientRequestConfiguration configuration = new NettyHttpClientRequestConfiguration.Builder()
				.readTimeout(Duration.ofMillis(2000))
					.build();

		nettyHttpClientRequestFactory = new NettyHttpClientRequestFactory(configuration);

		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/slow"))
			.respond(response()
				.withDelay(TimeUnit.MILLISECONDS, 3000));

		expectedException.expect(isA(HttpClientException.class));
		expectedException.expectCause(deeply(ReadTimeoutException.class));

		nettyHttpClientRequestFactory.createOf(new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET"))
			.execute();
	}

	@Test
	public void shouldThrowExceptionOnTimeoutAsync() throws Exception {
		NettyHttpClientRequestConfiguration configuration = new NettyHttpClientRequestConfiguration.Builder()
				.readTimeout(Duration.ofMillis(2000))
					.build();

		nettyHttpClientRequestFactory = new NettyHttpClientRequestFactory(configuration);

		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/slow"))
			.respond(response()
				.withDelay(TimeUnit.MILLISECONDS, 3000));

		expectedException.expect(ExecutionException.class);
		expectedException.expect(deeply(ReadTimeoutException.class));

		CompletableFuture<HttpResponseMessage> responseAsFuture = nettyHttpClientRequestFactory
				.createAsyncOf(new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET"))
					.executeAsync();

		Thread.sleep(3000);

		assertTrue(responseAsFuture.isCompletedExceptionally());

		responseAsFuture.get();
	}

	@Test
	public void shouldThrowExceptionOnTimeoutByAnnotation() {
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

		expectedException.expect(isA(HttpClientException.class));
		expectedException.expectCause(deeply(ReadTimeoutException.class));

		EndpointRequest request = new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET", new Headers(), null, void.class, null,
				new EndpointRequestMetadata(Arrays.asList(timeout)));

		nettyHttpClientRequestFactory.createOf(request).execute();
	}

	@Test
	public void shouldThrowExceptionOnTimeoutByAnnotationAsync() throws Exception {
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
		expectedException.expect(deeply(ReadTimeoutException.class));

		EndpointRequest request = new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET", new Headers(), null, void.class, null,
				new EndpointRequestMetadata(Arrays.asList(timeout)));

		CompletableFuture<HttpResponseMessage> responseAsFuture = nettyHttpClientRequestFactory
				.createAsyncOf(request)
					.executeAsync();

		Thread.sleep(3000);

		assertTrue(responseAsFuture.isCompletedExceptionally());

		responseAsFuture.get();
	}

	@Test
	public void shouldSendSecureRequest() throws Exception {
		mockServerClient = new MockServerClient("localhost", 7084);

		char[] keyStorePassword = SSLFactory.KEY_STORE_PASSWORD.toCharArray();

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(SSLFactory.getInstance().buildKeyStore(), keyStorePassword);

		SslContext sslContext = SslContextBuilder.forClient().build();

		NettyHttpClientRequestConfiguration configuration = new NettyHttpClientRequestConfiguration.Builder()
				.sslContext(sslContext)
					.build();

		nettyHttpClientRequestFactory = new NettyHttpClientRequestFactory(configuration);

		HttpRequest secureRequest = request()
				.withMethod("GET")
				.withPath("/secure")
				.withSecure(true);

		String responseBody = "{\"name\": \"Tiago de Freitas Lima\",\"age\":31}";

		mockServerClient
			.when(secureRequest)
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json(responseBody)));

		HttpResponseMessage response = nettyHttpClientRequestFactory.createOf(new EndpointRequest(URI.create("https://localhost:7084/secure"), "GET"))
				.execute();

		assertEquals(responseBody, new InputStreamContent(response.body()).asString());
		assertEquals("application/json", response.headers().get("Content-Type").get().value());
		assertEquals(StatusCode.ok(), response.status());

		mockServerClient.verify(secureRequest);
	}

	@Test
	public void shouldSendSecureRequestAsync() throws Exception {
		mockServerClient = new MockServerClient("localhost", 7084);

		char[] keyStorePassword = SSLFactory.KEY_STORE_PASSWORD.toCharArray();

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(SSLFactory.getInstance().buildKeyStore(), keyStorePassword);

		SslContext sslContext = SslContextBuilder.forClient().build();

		NettyHttpClientRequestConfiguration configuration = new NettyHttpClientRequestConfiguration.Builder()
				.sslContext(sslContext)
					.build();

		nettyHttpClientRequestFactory = new NettyHttpClientRequestFactory(configuration);

		HttpRequest secureRequest = request()
				.withMethod("GET")
				.withPath("/secure")
				.withSecure(true);

		String responseBody = "{\"name\": \"Tiago de Freitas Lima\",\"age\":31}";

		mockServerClient
			.when(secureRequest)
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json(responseBody)));

		CompletableFuture<HttpResponseMessage> responseAsFuture = nettyHttpClientRequestFactory
				.createAsyncOf(new EndpointRequest(URI.create("https://localhost:7084/secure"), "GET"))
					.executeAsync();

		responseAsFuture.thenAccept(response -> {
			assertEquals(responseBody, new InputStreamContent(response.body()).asString());
			assertEquals("application/json", response.headers().get("Content-Type").get().value());
			assertEquals(StatusCode.ok(), response.status());

			mockServerClient.verify(secureRequest);
		});
	}
	
	private Matcher<? extends Throwable> deeply(Class<? extends Throwable> expectedCause) {
		return new BaseMatcher<Throwable>() {
			@Override
			public boolean matches(Object argument) {
				Throwable exception = (Throwable) argument;
				Throwable cause = exception.getCause();

				while (cause != null) {
					if (expectedCause.isInstance(cause)) return true;
					cause = cause.getCause();
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(expectedCause.getName());
			}
		};
	}

}
