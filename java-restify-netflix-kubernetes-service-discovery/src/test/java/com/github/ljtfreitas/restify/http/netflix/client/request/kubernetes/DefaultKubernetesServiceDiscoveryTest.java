package com.github.ljtfreitas.restify.http.netflix.client.request.kubernetes;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.StringBody.exact;
import static org.mockserver.verify.VerificationTimes.once;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
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
import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.LoadBalancerBuilder;

import io.fabric8.kubernetes.api.model.EndpointsBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;

public class DefaultKubernetesServiceDiscoveryTest {

	@Rule
	public MockServerRule mockApiServerRule = new MockServerRule(this, 7080, 7081, 7082);

	@ClassRule
	public static KubernetesServer kubernetesMockServer = new KubernetesServer(false);

	private MyApi myApi;

	private MockServerClient mockServerClient;

	private DefaultKubernetesServiceDiscovery kubernetesServiceDiscovery;

	@Before
	public void setup() throws Exception {
		KubernetesClient kubernetesClient = kubernetesMockServer.getClient();

		kubernetesServiceDiscovery = new DefaultKubernetesServiceDiscovery(kubernetesClient);

		KubernetesServers kubernetesServers = new KubernetesServers(kubernetesServiceDiscovery, "myApi");

		kubernetesMockServer.expect()
			.get()
			.withPath("/api/v1/namespaces/test/endpoints/myApi")
			.andReturn(200, new EndpointsBuilder()
					.withNewMetadata()
					.withName("myApp")
					.endMetadata()
					.addNewSubset()
					.addNewAddress()
						.withIp("localhost")
					.endAddress()
					.addNewPort("http", 7080, "TCP")
					.addNewPort("http", 7081, "TCP")
					.addNewPort("http", 7082, "TCP")
					.endSubset()
					.build())
			.always();

		ILoadBalancer loadBalancer = LoadBalancerBuilder.newBuilder()
				.withDynamicServerList(kubernetesServers)
					.buildDynamicServerListLoadBalancer();

		DefaultClientConfigImpl config = new DefaultClientConfigImpl();
		config.setClientName("myApp");

		RibbonHttpClientRequestFactory ribbonHttpClientRequestFactory = new RibbonHttpClientRequestFactory(loadBalancer, config);

		myApi = new RestifyProxyBuilder()
				.client(ribbonHttpClientRequestFactory)
				.target(MyApi.class, "http://myApi")
				.build();
	}

	@After
	public void after() throws Exception {
		kubernetesServiceDiscovery.close();
	}

	@Test
	public void shouldSendGetRequestOnJsonFormat() {
		mockServerClient = new MockServerClient("localhost", 7080);

		HttpRequest httpRequest = request()
			.withMethod("GET")
			.withPath("/json");

		mockServerClient
			.when(httpRequest)
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\": \"Tiago de Freitas Lima\",\"age\":31}")));

		MyModel myModel = myApi.json();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals(31, myModel.age);

		mockServerClient.verify(httpRequest, once());
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
