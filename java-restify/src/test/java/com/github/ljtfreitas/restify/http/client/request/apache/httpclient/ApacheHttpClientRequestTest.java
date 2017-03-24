package com.github.ljtfreitas.restify.http.client.request.apache.httpclient;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.http.client.config.RequestConfig;
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
import com.github.ljtfreitas.restify.http.client.request.apache.httpclient.ApacheHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.contract.BodyParameter;
import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.Post;

public class ApacheHttpClientRequestTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MyApi myApi;

	private MockServerClient mockServerClient;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		RequestConfig configuration = RequestConfig.custom()
				.setSocketTimeout(2000)
					.build();

		ApacheHttpClientRequestFactory apacheHttpClientRequestFactory = new ApacheHttpClientRequestFactory(configuration);

		myApi = new RestifyProxyBuilder()
				.client(apacheHttpClientRequestFactory)
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
					.withHeader("Content-Type", "application/json; charset=UTF-8")
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
			.withHeader("Content-Type", "application/json; charset=UTF-8")
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
	public void shouldSendGetRequestOnXmlFormat() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/xml"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/xml; charset=UTF-8")
					.withBody(exact("<model><name>Tiago de Freitas Lima</name><age>31</age></model>")));

		MyModel myModel = myApi.xml();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);
	}

	@Test
	public void shouldSendPostRequestOnXmlFormat() {
		HttpRequest httpRequest = request()
			.withMethod("POST")
			.withPath("/xml")
			.withHeader("Content-Type", "application/xml; charset=UTF-8")
			.withBody(exact("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><model><name>Tiago de Freitas Lima</name><age>31</age></model>"));

		mockServerClient
			.when(httpRequest)
			.respond(response()
				.withStatusCode(201)
				.withHeader("Content-Type", "text/plain")
				.withBody(exact("OK")));

		myApi.xml(new MyModel("Tiago de Freitas Lima", 31));

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
		expectedException.expectCause(isA(SocketTimeoutException.class));

		myApi.json();
	}

	interface MyApi {

		@Path("/json") @Get
		public MyModel json();

		@Path("/json") @Post
		@Header(name = "Content-Type", value = "application/json")
		public void json(@BodyParameter MyModel myModel);

		@Path("/xml") @Get
		public MyModel xml();

		@Path("/xml") @Post
		@Header(name = "Content-Type", value = "application/xml")
		public void xml(@BodyParameter MyModel myModel);
	}

	@XmlRootElement(name = "model")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class MyModel {

		@JsonProperty
		String name;

		@JsonProperty
		int age;

		public MyModel() {
		}

		public MyModel(@JsonProperty String name, @JsonProperty int age) {
			this.name = name;
			this.age = age;
		}

	}
}
