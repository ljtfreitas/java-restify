package com.github.ljtfreitas.restify.http.netflix.client.request;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.net.URI;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JacksonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.JaxbXmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.DefaultEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

public class RibbonHttpClientRequestFactoryTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080, 7081, 7082);

	private MockServerClient mockServerClient;

	private EndpointRequestExecutor requestExecutor;

	@Before
	public void setup() {
		ILoadBalancer loadBalancer = new BaseLoadBalancer();
		loadBalancer.addServers(Arrays.asList(new Server("localhost", 7080)));
		loadBalancer.addServers(Arrays.asList(new Server("localhost", 7081)));
		loadBalancer.addServers(Arrays.asList(new Server("localhost", 7082)));

		IClientConfig clientConfig = new DefaultClientConfigImpl();

		RibbonHttpClientRequestFactory ribbonHttpClientRequestFactory = new RibbonHttpClientRequestFactory(loadBalancer, clientConfig);

		HttpMessageConverters messageConverters = new HttpMessageConverters(Arrays.asList(new JacksonMessageConverter<>(), new JaxbXmlMessageConverter<>()));

		requestExecutor = new DefaultEndpointRequestExecutor(ribbonHttpClientRequestFactory, new EndpointRequestWriter(messageConverters),
				new EndpointResponseReader(messageConverters));
	}

	@Test
	public void shouldSendGetRequestOnJsonFormat() {
		mockServerClient = new MockServerClient("localhost", 7080);

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/json"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\": \"Tiago de Freitas Lima\",\"age\":31}")));

		EndpointResponse<MyModel> response = requestExecutor.execute(new EndpointRequest(URI.create("http://myApi/json"), "GET", MyModel.class));

		MyModel myModel = response.body();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);
	}

	@Test
	public void shouldSendPostRequestOnJsonFormat() {
		mockServerClient = new MockServerClient("localhost", 7081);

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

		requestExecutor.execute(new EndpointRequest(URI.create("http://myApi/json"), "POST",
				new Headers(Arrays.asList(Header.contentType("application/json"))), new MyModel("Tiago de Freitas Lima", 31)));

		mockServerClient.verify(httpRequest, once());
	}

	@Test
	public void shouldSendGetRequestOnXmlFormat() {
		mockServerClient = new MockServerClient("localhost", 7082);

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/xml"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/xml")
					.withBody(exact("<model><name>Tiago de Freitas Lima</name><age>31</age></model>")));

		EndpointResponse<MyModel> response = requestExecutor.execute(new EndpointRequest(URI.create("http://myApi/xml"), "GET", MyModel.class));

		MyModel myModel = response.body();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);
	}

	@Test
	public void shouldSendPostRequestOnXmlFormat() {
		mockServerClient = new MockServerClient("localhost", 7080);

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

		requestExecutor.execute(new EndpointRequest(URI.create("http://myApi/xml"), "POST",
				new Headers(Arrays.asList(Header.contentType("application/xml"))), new MyModel("Tiago de Freitas Lima", 31)));

		mockServerClient.verify(httpRequest, once());
	}

	@XmlRootElement(name = "model")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class MyModel {

		@JsonProperty
		String name;

		@JsonProperty
		int age;

		private MyModel() {
		}

		private MyModel(String name, int age) {
			this.name = name;
			this.age = age;
		}

	}

}
