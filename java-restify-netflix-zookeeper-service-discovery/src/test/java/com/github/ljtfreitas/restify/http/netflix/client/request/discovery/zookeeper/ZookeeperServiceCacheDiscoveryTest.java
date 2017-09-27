package com.github.ljtfreitas.restify.http.netflix.client.request.discovery.zookeeper;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.contract.BodyParameter;
import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.Post;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.netflix.client.request.discovery.DiscoveryServerList;
import com.github.ljtfreitas.restify.http.netflix.client.request.discovery.ServiceFailureExceptionHandler;
import com.github.ljtfreitas.restify.http.netflix.client.request.discovery.zookeeper.ZookeeperServiceRegistryRequest.Payload;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.LoadBalancerBuilder;

public class ZookeeperServiceCacheDiscoveryTest {

	@Rule
	public MockServerRule mockApiServerRule = new MockServerRule(this, 7080, 7081, 7082);

	private TestingServer zookeeperServer;

	private MyApi myApi;

	private MockServerClient mockServerClient;

	private ZookeeperServiceCacheDiscovery<ZookeeperServiceInstance> zookeeperServiceDiscovery;

	private ZookeeperServiceRegistry<ZookeeperServiceInstance> zookeeperServiceRegister;

	@Before
	public void setup() throws Exception {
		zookeeperServer = new TestingServer(2181, true);

		ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration("localhost", 2181, "/services");

		ZookeeperCuratorServiceDiscovery<ZookeeperServiceInstance> zookeeperCuratorServiceDiscovery = new ZookeeperCuratorServiceDiscovery<>(ZookeeperServiceInstance.class, zookeeperConfiguration, new ZookeeperInstanceSerializer());

		zookeeperServiceRegister = new DefaultZookeeperServiceRegistry<>(zookeeperCuratorServiceDiscovery);

		ZookeeperServiceInstance zookeeperServiceInstance = new ZookeeperServiceInstance("myApi", "localhost", 7080);
		zookeeperServiceRegister.register(ZookeeperServiceRegistryRequest.registry(zookeeperServiceInstance).with(Payload.of(zookeeperServiceInstance)));

		zookeeperServiceInstance = new ZookeeperServiceInstance("myApi", "localhost", 7081);
		zookeeperServiceRegister.register(ZookeeperServiceRegistryRequest.registry(zookeeperServiceInstance).with(Payload.of(zookeeperServiceInstance)));

		zookeeperServiceInstance = new ZookeeperServiceInstance("myApi", "localhost", 7082);
		zookeeperServiceRegister.register(ZookeeperServiceRegistryRequest.registry(zookeeperServiceInstance).with(Payload.of(zookeeperServiceInstance)));

		zookeeperServiceDiscovery = new ZookeeperServiceCacheDiscovery<>("myApi", zookeeperCuratorServiceDiscovery);

		DiscoveryServerList<ZookeeperServiceInstance> zookeeperServers = new DiscoveryServerList<>(zookeeperServiceDiscovery);

		ILoadBalancer loadBalancer = LoadBalancerBuilder.newBuilder()
				.withDynamicServerList(zookeeperServers)
					.buildDynamicServerListLoadBalancer();

		RibbonHttpClientRequestFactory ribbonHttpClientRequestFactory = new RibbonHttpClientRequestFactory(loadBalancer, new ServiceFailureExceptionHandler(zookeeperServiceDiscovery));

		myApi = new RestifyProxyBuilder()
				.client(ribbonHttpClientRequestFactory)
				.target(MyApi.class, "http://myApi")
				.build();
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

		MyModel myModel = myApi.json();

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

		myApi.json(new MyModel("Tiago de Freitas Lima", 31));

		mockServerClient.verify(httpRequest, once());
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
