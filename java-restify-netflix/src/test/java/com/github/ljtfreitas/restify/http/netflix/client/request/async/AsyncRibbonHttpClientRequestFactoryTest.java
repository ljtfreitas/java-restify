package com.github.ljtfreitas.restify.http.netflix.client.request.async;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JacksonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.JaxbXmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.async.DefaultAsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

public class AsyncRibbonHttpClientRequestFactoryTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080, 7081, 7082);

	private MockServerClient mockServerClient;

	private AsyncEndpointRequestExecutor requestExecutor;

	private ExecutorService executor;

	@Before
	public void setup() {
		ILoadBalancer loadBalancer = new BaseLoadBalancer();
		loadBalancer.addServers(Arrays.asList(new Server("localhost", 7080)));
		loadBalancer.addServers(Arrays.asList(new Server("localhost", 7081)));
		loadBalancer.addServers(Arrays.asList(new Server("localhost", 7082)));

		IClientConfig clientConfig = new DefaultClientConfigImpl();

		SimpleAsyncHttpClientRequestFactory delegate = new SimpleAsyncHttpClientRequestFactory();

		AsyncRibbonHttpClientRequestFactory asyncRibbonHttpClientRequestFactory = new AsyncRibbonHttpClientRequestFactory(delegate, loadBalancer, clientConfig);

		HttpMessageConverters messageConverters = new HttpMessageConverters(Arrays.asList(new JacksonMessageConverter<>(), new JaxbXmlMessageConverter<>()));

		executor = Executors.newCachedThreadPool();

		requestExecutor = new DefaultAsyncEndpointRequestExecutor(executor, asyncRibbonHttpClientRequestFactory, new EndpointRequestWriter(messageConverters),
				new EndpointResponseReader(messageConverters), null);
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

		CompletableFuture<EndpointResponse<MyModel>> future = requestExecutor.executeAsync(new EndpointRequest(URI.create("http://myApi/json"), "GET", MyModel.class));

		EndpointResponse<MyModel> response = future.join();

		MyModel myModel = response.body();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);
	}

	@Test
	public void shouldSendPostRequestOnJsonFormat() throws Exception {
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

		requestExecutor.executeAsync(new EndpointRequest(URI.create("http://myApi/json"), "POST",
				new Headers(Arrays.asList(Header.contentType("application/json"))), new MyModel("Tiago de Freitas Lima", 31)));

		Thread.sleep(1000);

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

		CompletableFuture<EndpointResponse<MyModel>> future = requestExecutor
				.executeAsync(new EndpointRequest(URI.create("http://myApi/xml"), "GET", MyModel.class));

		EndpointResponse<MyModel> response = future.join();

		MyModel myModel = response.body();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);
	}

	@Test
	public void shouldSendPostRequestOnXmlFormat() throws Exception {
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

		requestExecutor.executeAsync(new EndpointRequest(URI.create("http://myApi/xml"), "POST",
				new Headers(Arrays.asList(Header.contentType("application/xml"))), new MyModel("Tiago de Freitas Lima", 31)));

		Thread.sleep(1000);

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

	private class SimpleAsyncHttpClientRequestFactory implements AsyncHttpClientRequestFactory {

		private final HttpClientRequestFactory factory = new JdkHttpClientRequestFactory();

		@Override
		public HttpClientRequest createOf(EndpointRequest endpointRequest) {
			return factory.createOf(endpointRequest);
		}

		@Override
		public AsyncHttpClientRequest createAsyncOf(EndpointRequest endpointRequest) {
			return new SimpleAsyncHttpClientRequest(factory.createOf(endpointRequest));
		}
	}

	private class SimpleAsyncHttpClientRequest implements AsyncHttpClientRequest {

		private final HttpClientRequest source;

		private SimpleAsyncHttpClientRequest(HttpClientRequest source) {
			this.source = source;
		}

		@Override
		public HttpResponseMessage execute() throws HttpClientException {
			return source.execute();
		}

		@Override
		public URI uri() {
			return source.uri();
		}

		@Override
		public String method() {
			return source.method();
		}

		@Override
		public OutputStream output() {
			return source.output();
		}

		@Override
		public Charset charset() {
			return source.charset();
		}

		@Override
		public HttpRequestMessage replace(Header header) {
			return new SimpleAsyncHttpClientRequest((HttpClientRequest) source.replace(header));
		}

		@Override
		public Headers headers() {
			return source.headers();
		}

		@Override
		public CompletableFuture<HttpResponseMessage> executeAsync() throws HttpClientException {
			return CompletableFuture.supplyAsync(() -> source.execute());
		}

	}

}
