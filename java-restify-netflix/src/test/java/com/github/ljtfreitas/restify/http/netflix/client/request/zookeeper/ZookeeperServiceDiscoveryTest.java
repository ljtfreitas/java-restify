package com.github.ljtfreitas.restify.http.netflix.client.request.zookeeper;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import java.util.Collections;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;

import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.contract.BodyParameter;
import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.Post;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonLoadBalancedClient;
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.LoadBalancerBuilder;
import com.netflix.loadbalancer.PingUrl;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAffinityServerListFilter;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;

public class ZookeeperServiceDiscoveryTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080, 7081, 7082);

	private MyApi myApi;

	private MockServerClient mockServerClient;

	private ZookeeperServiceDiscovery zookeeperServiceDiscovery;

	@Before
	public void setup() throws Exception {
		DefaultClientConfigImpl ribbonLoadBalacerConfiguration = new DefaultClientConfigImpl();
		ribbonLoadBalacerConfiguration.setClientName("myApp");

		ZoneAvoidanceRule rule = new ZoneAvoidanceRule();
		rule.initWithNiwsConfig(ribbonLoadBalacerConfiguration);

		ZoneAffinityServerListFilter<Server> predicate = new ZoneAffinityServerListFilter<>(ribbonLoadBalacerConfiguration);

		IPing ping = new PingUrl();

		ZookeeperDiscoveryConfiguration zookeeperDiscoveryConfiguration = new ZookeeperDiscoveryConfiguration();
		zookeeperDiscoveryConfiguration.root("/services").serviceName("myApp");

		CuratorFramework curator = CuratorFrameworkFactory.builder()
				.connectString("localhost:2181")
				.retryPolicy(new RetryNTimes(0, 0))
					.build();
		curator.start();

		InstanceSerializer<ZookeeperInstance> serializer = new JsonInstanceSerializer<>(ZookeeperInstance.class);

		zookeeperServiceDiscovery = new ZookeeperServiceDiscovery(zookeeperDiscoveryConfiguration, curator, serializer);
		zookeeperServiceDiscovery.register(new ZookeeperInstance("myApp", "localhost", 7080, Collections.emptyMap()));
		zookeeperServiceDiscovery.register(new ZookeeperInstance("myApp", "localhost", 7081, Collections.emptyMap()));
		zookeeperServiceDiscovery.register(new ZookeeperInstance("myApp", "localhost", 7082, Collections.emptyMap()));

		ZookeeperServers zookeeperServers = new ZookeeperServers("myApp", zookeeperServiceDiscovery);

		ZoneAwareLoadBalancer<Server> loadBalancer = LoadBalancerBuilder.newBuilder()
				.withClientConfig(ribbonLoadBalacerConfiguration)
				.withRule(rule)
				.withPing(ping)
				.withDynamicServerList(zookeeperServers)
				.withServerListFilter(predicate)
					.buildDynamicServerListLoadBalancer();

		JdkHttpClientRequestFactory delegate = new JdkHttpClientRequestFactory();

		RibbonLoadBalancedClient ribbonLoadBalancedClient = new RibbonLoadBalancedClient(loadBalancer, ribbonLoadBalacerConfiguration, delegate);

		RibbonHttpClientRequestFactory ribbonHttpClientRequestFactory = new RibbonHttpClientRequestFactory(ribbonLoadBalancedClient);

		myApi = new RestifyProxyBuilder()
				.client(ribbonHttpClientRequestFactory)
				.target(MyApi.class, "http://myApi")
				.build();
	}

	@After
	public void after() {
		zookeeperServiceDiscovery.close();
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

	interface MyApi {

		@Path("/json") @Get
		public MyModel json();

		@Path("/json") @Post
		@Header(name = "Content-Type", value = "application/json")
		public void json(@BodyParameter MyModel myModel);
	}

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

}
