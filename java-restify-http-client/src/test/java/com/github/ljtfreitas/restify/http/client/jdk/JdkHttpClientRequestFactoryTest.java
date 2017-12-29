package com.github.ljtfreitas.restify.http.client.jdk;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

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
import com.github.ljtfreitas.restify.http.client.request.Timeout;

public class JdkHttpClientRequestFactoryTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080, 7084);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MockServerClient mockServerClient;

	private JdkHttpClientRequestFactory jdkHttpClientRequestFactory;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);
		
		jdkHttpClientRequestFactory = new JdkHttpClientRequestFactory();
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

		HttpResponseMessage response = jdkHttpClientRequestFactory.createOf(new EndpointRequest(URI.create("http://localhost:7080/json"), "GET"))
				.execute();

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

		JdkHttpClientRequest request = jdkHttpClientRequestFactory.createOf(endpointRequest);
		request.output().write(requestBody.getBytes());
		request.output().flush();

		HttpResponseMessage response = request.execute();

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

		HttpClientRequestConfiguration configuration = new HttpClientRequestConfiguration.Builder()
				.connectionTimeout(2000)
				.readTimeout(2000)
				.build();

		jdkHttpClientRequestFactory = new JdkHttpClientRequestFactory(configuration);

		expectedException.expect(isA(HttpClientException.class));
		expectedException.expectCause(isA(SocketTimeoutException.class));

		jdkHttpClientRequestFactory.createOf(new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET"))
			.execute();
	}

	@Test
	public void shouldThrowExceptionOnTimeoutWithAnnotation() {
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
		expectedException.expectCause(isA(SocketTimeoutException.class));

		EndpointRequest request = new EndpointRequest(URI.create("http://localhost:7080/slow"), "GET", new Headers(), null,	void.class,
				null, new EndpointRequestMetadata(Arrays.asList(timeout)));

		jdkHttpClientRequestFactory.createOf(request).execute();
	}

	@Test
	public void shouldSendSecureRequest() throws Exception {
		mockServerClient = new MockServerClient("localhost", 7084);

		char[] keyStorePassword = SSLFactory.KEY_STORE_PASSWORD.toCharArray();

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(SSLFactory.getInstance().buildKeyStore(), keyStorePassword);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

		HttpClientRequestConfiguration configuration = new HttpClientRequestConfiguration.Builder()
				.ssl()
					.sslSocketFactory(sslContext.getSocketFactory())
				.build();

		jdkHttpClientRequestFactory = new JdkHttpClientRequestFactory(configuration);

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

		HttpResponseMessage response = jdkHttpClientRequestFactory.createOf(new EndpointRequest(URI.create("https://localhost:7084/secure"), "GET"))
				.execute();

		assertEquals(responseBody, new InputStreamContent(response.body()).asString());
		assertEquals("application/json", response.headers().get("Content-Type").get().value());
		assertEquals(StatusCode.ok(), response.status());

		mockServerClient.verify(secureRequest);
	}

	@Test
	public void shouldSendRequestWithConfiguredProxy() {
		HttpClientRequestConfiguration configuration = new HttpClientRequestConfiguration.Builder()
				.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 7080)))
					.build();

		jdkHttpClientRequestFactory = new JdkHttpClientRequestFactory(configuration);

		String responseBody = "{\"name\": \"Tiago de Freitas Lima\",\"age\":31}";

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/proxified"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json(responseBody)));

		HttpResponseMessage response = jdkHttpClientRequestFactory.createOf(new EndpointRequest(URI.create("http://www.google.com.br"), "GET"))
				.execute();

		assertEquals(responseBody, new InputStreamContent(response.body()).asString());
		assertEquals("application/json", response.headers().get("Content-Type").get().value());
		assertEquals(StatusCode.ok(), response.status());
	}
}
