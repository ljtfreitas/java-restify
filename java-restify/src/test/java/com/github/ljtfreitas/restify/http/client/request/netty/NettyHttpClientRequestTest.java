package com.github.ljtfreitas.restify.http.client.request.netty;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.RestifyHttpException;
import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.contract.BodyParameter;
import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.Post;

import io.netty.handler.timeout.ReadTimeoutException;

public class NettyHttpClientRequestTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MyApi myApi;

	private MockServerClient mockServerClient;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		NettyHttpClientRequestConfiguration nettyHttpClientRequestConfiguration = new NettyHttpClientRequestConfiguration.Builder()
				.readTimeout(Duration.ofMillis(2000))
					.build();

		NettyHttpClientRequestFactory nettyHttpClientRequestFactory = new NettyHttpClientRequestFactory(nettyHttpClientRequestConfiguration);

		myApi = new RestifyProxyBuilder()
				.client(nettyHttpClientRequestFactory)
				.target(MyApi.class, "http://localhost:7080")
				.build();
	}

	@Test
	public void shouldSendGetRequestOnJsonFormat() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\": \"Tiago de Freitas Lima\",\"age\":31}")));

		MyModel myModel = myApi.json();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);
	}

	@Test
	public void shouldSendPostRequestOnJsonFormat() {
		HttpRequest httpRequest = request()
			.withMethod("POST")
			.withPath("/json")
			.withHeader("Content-Type", "application/json")
			.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"age\":31}"));

		mockServerClient
			.when(httpRequest)
			.respond(response()
				.withStatusCode(201)
				.withHeader("Content-Type", "text/plain")
				.withBody(exact("OK")));

		myApi.json(new MyModel("Tiago de Freitas Lima", 31));

		mockServerClient.verify(httpRequest, once());
	}

	@Test
	public void shouldThrowExceptionOnTimeout() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/json"))
			.respond(response()
				.withDelay(TimeUnit.MILLISECONDS, 3000));

		expectedException.expect(isA(RestifyHttpException.class));
		expectedException.expectCause(deeply(ReadTimeoutException.class));

		myApi.json();
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

	interface MyApi {

		@Path("/json") @Get
		public MyModel json();

		@Path("/json") @Post
		@Header(name = "Content-Type", value = "application/json")
		public void json(@BodyParameter MyModel myModel);
	}

	public static class MyModel {

		@JsonProperty
		String name;

		@JsonProperty
		int age;

		public MyModel() {
		}

		public MyModel(String name, int age) {
			this.name = name;
			this.age = age;
		}
	}
}
