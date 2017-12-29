package com.github.ljtfreitas.restify.http.netflix.client.request.discovery.zookeeper;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.net.URI;
import java.util.Arrays;

import org.apache.curator.test.TestingServer;
import org.junit.After;
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
import com.github.ljtfreitas.restify.http.client.request.DefaultEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.netflix.client.request.discovery.DiscoveryServerList;
import com.github.ljtfreitas.restify.http.netflix.client.request.discovery.ServiceFailureExceptionHandler;
import com.github.ljtfreitas.restify.http.netflix.client.request.discovery.zookeeper.ZookeeperServiceRegistryRequest.Payload;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.LoadBalancerBuilder;

public class DefaultZookeeperServiceDiscoveryTest {

	@Rule
	public MockServerRule mockApiServerRule = new MockServerRule(this, 7080, 7081, 7082);

	private TestingServer zookeeperServer;

	private MockServerClient mockServerClient;

	private EndpointRequestExecutor requestExecutor;

	private DefaultZookeeperServiceDiscovery<ZookeeperServiceInstance> zookeeperServiceDiscovery;

	private ZookeeperServiceRegistry<ZookeeperServiceInstance> zookeeperServiceRegister;

	@Before
	public void setup() throws Exception {
		zookeeperServer = new TestingServer(2181, true);

		ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(ZookeeperQuorum.of("localhost:2181"), "/services");

		ZookeeperCuratorServiceDiscovery<ZookeeperServiceInstance> zookeeperCuratorServiceDiscovery = new ZookeeperCuratorServiceDiscovery<>(ZookeeperServiceInstance.class, zookeeperConfiguration, new ZookeeperInstanceSerializer());

		zookeeperServiceDiscovery = new DefaultZookeeperServiceDiscovery<>(zookeeperCuratorServiceDiscovery);

		zookeeperServiceRegister = new DefaultZookeeperServiceRegistry<>(zookeeperCuratorServiceDiscovery);

		ZookeeperServiceInstance zookeeperServiceInstance = new ZookeeperServiceInstance("myApi", "localhost", 7080);
		zookeeperServiceRegister.register(ZookeeperServiceRegistryRequest.registry(zookeeperServiceInstance).with(Payload.of(zookeeperServiceInstance)));

		zookeeperServiceInstance = new ZookeeperServiceInstance("myApi", "localhost", 7081);
		zookeeperServiceRegister.register(ZookeeperServiceRegistryRequest.registry(zookeeperServiceInstance).with(Payload.of(zookeeperServiceInstance)));

		zookeeperServiceInstance = new ZookeeperServiceInstance("myApi", "localhost", 7082);
		zookeeperServiceRegister.register(ZookeeperServiceRegistryRequest.registry(zookeeperServiceInstance).with(Payload.of(zookeeperServiceInstance)));

		DiscoveryServerList<ZookeeperServiceInstance> zookeeperServers = new DiscoveryServerList<>(zookeeperServiceDiscovery, "myApi");

		ILoadBalancer loadBalancer = LoadBalancerBuilder.newBuilder()
				.withDynamicServerList(zookeeperServers)
					.buildDynamicServerListLoadBalancer();

		RibbonHttpClientRequestFactory ribbonHttpClientRequestFactory = new RibbonHttpClientRequestFactory(loadBalancer, new ServiceFailureExceptionHandler(zookeeperServiceDiscovery));

		HttpMessageConverters messageConverters = new HttpMessageConverters(Arrays.asList(new JacksonMessageConverter<>()));

		requestExecutor = new DefaultEndpointRequestExecutor(ribbonHttpClientRequestFactory, new EndpointRequestWriter(messageConverters),
				new EndpointResponseReader(messageConverters));
	}

	@After
	public void after() throws Exception {
		zookeeperServiceDiscovery.close();
		zookeeperServiceRegister.close();
		zookeeperServer.close();
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

	private static class MyModel {

		@JsonProperty
		String name;

		@JsonProperty
		int age;

		@SuppressWarnings("unused")
		private MyModel() {
		}

		MyModel(String name, int age) {
			this.name = name;
			this.age = age;
		}

	}

}
