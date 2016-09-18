package com.restify.http.client.jdk;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;

import com.restify.http.RestifyProxyBuilder;
import com.restify.http.contract.BodyParameter;
import com.restify.http.contract.Get;
import com.restify.http.contract.Header;
import com.restify.http.contract.Path;
import com.restify.http.contract.Post;

public class JdkHttpClientRequestTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	private MyApi myApi;

	private MyModelJsonApi myModelJsonApi;

	private MockServerClient mockServerClient;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		myApi = new RestifyProxyBuilder()
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
	}

	@XmlRootElement(name = "model")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class MyModel {

		String name;
		int age;

		public MyModel() {
		}

		public MyModel(String name, int age) {
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
