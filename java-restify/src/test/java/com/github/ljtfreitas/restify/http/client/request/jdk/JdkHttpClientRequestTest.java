package com.github.ljtfreitas.restify.http.client.request.jdk;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.socket.SSLFactory;
import org.mockserver.verify.VerificationTimes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.RestifyHttpException;
import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.client.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;
import com.github.ljtfreitas.restify.http.contract.BodyParameter;
import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.Post;

public class JdkHttpClientRequestTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080, 7084);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MyApi myApi;

	private MyModelJsonApi myModelJsonApi;

	private MockServerClient mockServerClient;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		myApi = new RestifyProxyBuilder()
				.client()
					.connectionTimeout(2000)
					.readTimeout(2000)
					.and()
				.target(MyApi.class, "http://localhost:7080")
				.build();

		myModelJsonApi = new RestifyProxyBuilder()
				.target(MyModelJsonApi.class, "http://localhost:7080")
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
	public void shouldGetResponseBodyOnStreamObject() {
		String responseBodyAsJson = "{\"name\": \"Tiago de Freitas Lima\",\"age\":31}";

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json(responseBodyAsJson)));

		InputStream responseBody = myApi.jsonAsStream();

		BufferedReader buffer = new BufferedReader(new InputStreamReader(responseBody));
        String output = buffer.lines().collect(Collectors.joining("\n"));

        assertEquals(responseBodyAsJson, output);
	}

	@Test
	public void shouldGetResponseBodyOnByteArray() {
		String responseBodyAsJson = "{\"name\": \"Tiago de Freitas Lima\",\"age\":31}";

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json(responseBodyAsJson)));

		byte[] responseBody = myApi.jsonAsBytes();

        assertEquals(responseBodyAsJson, new String(responseBody));
	}

	@Test
	public void shouldSendGetRequestOnJsonFormatWithExtendedApi() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\": \"Tiago de Freitas Lima\",\"age\":31}")));

		MyModel myModel = myModelJsonApi.json();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);
	}

	@Test
	public void shouldGetCollectionOfJsonResponse() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/json/all"))
		.respond(response()
				.withStatusCode(200)
				.withHeader("Content-Type", "application/json")
				.withBody(json("[{\"name\": \"Tiago de Freitas Lima 1\",\"age\":31},{\"name\": \"Tiago de Freitas Lima 2\",\"age\":32}]")));

		Collection<MyModel> myModelCollection = myApi.jsonCollection();

		assertEquals(2, myModelCollection.size());
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
	public void shouldSendGetRequestOnXmlFormat() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/xml"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/xml")
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
			.withHeader("Content-Type", "application/xml")
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

	@Test
	public void shouldSendSecureRequest() throws Exception {
		mockServerClient = new MockServerClient("localhost", 7084);

		char[] keyStorePassword = SSLFactory.KEY_STORE_PASSWORD.toCharArray();

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(SSLFactory.getInstance().buildKeyStore(), keyStorePassword);

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

		myApi = new RestifyProxyBuilder()
				.client()
					.ssl()
						.sslSocketFactory(sslContext.getSocketFactory())
					.and()
				.target(MyApi.class, "https://localhost:7084")
				.build();

		HttpRequest secureRequest = request()
				.withMethod("GET")
				.withPath("/json")
				.withSecure(true);

		mockServerClient
			.when(secureRequest)
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\": \"Tiago de Freitas Lima\",\"age\":31}")));

		MyModel myModel = myApi.json();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);

		mockServerClient.verify(secureRequest);
	}

	@Test
	public void shouldSendRequestWithConfiguredProxy() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\": \"Tiago de Freitas Lima\",\"age\":31}")));

		myApi = new RestifyProxyBuilder()
				.client()
					.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 7080)))
					.and()
				.target(MyApi.class, "http://www.google.com")
				.build();

		MyModel myModel = myApi.json();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);
	}

	@Test
	public void shouldRetryRequest() {
		HttpRequest errorRequest = request()
			.withMethod("GET")
			.withPath("/retry");

		mockServerClient
			.when(errorRequest)
			.respond(response()
					.withStatusCode(500));

		myApi = new RestifyProxyBuilder()
				.retry()
					.configure()
						.attempts(3)
						.when(HttpStatusCode.INTERNAL_SERVER_ERROR)
						.and()
				.target(MyApi.class, "http://localhost:7080")
				.build();

		StatusCode status = myApi.retry();

		assertTrue(status.isServerError());

		mockServerClient.verify(errorRequest, VerificationTimes.exactly(3));
	}

	interface MyApi {

		@Path("/json") @Get
		public MyModel json();

		@Path("/json") @Post
		@Header(name = "Content-Type", value = "application/json")
		public void json(@BodyParameter MyModel myModel);

		@Path("/json/all") @Get
		public Collection<MyModel> jsonCollection();

		@Path("/xml") @Get
		public MyModel xml();

		@Path("/xml") @Post
		@Header(name = "Content-Type", value = "application/xml")
		public void xml(@BodyParameter MyModel myModel);

		@Path("/json") @Get
		public InputStream jsonAsStream();

		@Path("/json") @Get
		public byte[] jsonAsBytes();

		@Path("/retry") @Get
		public StatusCode retry();
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

	interface MyGenericJsonApi<T> {

		@Path("/json") @Get
		public T json();
	}

	interface MyModelJsonApi extends MyGenericJsonApi<MyModel> {
	}
}
