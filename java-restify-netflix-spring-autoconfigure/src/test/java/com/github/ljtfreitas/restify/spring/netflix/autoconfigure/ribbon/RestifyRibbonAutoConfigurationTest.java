package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.ribbon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.ljtfreitas.restify.spring.netflix.autoconfigure.ribbon.RestifyRibbonAutoConfigurationTest.SampleSpringApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleSpringApplication.class)
public class RestifyRibbonAutoConfigurationTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8080);

	private MockServerClient mockServerClient;

	@Autowired
	private ObjectProvider<LoadBalancedApi> provider;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8080);

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/get"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "text/plain")
					.withBody(json("Ribbon it's works!")));
	}
	
	@Test
	public void shouldCreateBeanOfLoadBalancedApiType() {
		LoadBalancedApi loadBalancedApi = provider.getIfAvailable();

		assertNotNull(loadBalancedApi);

		// request against http://localhost:8080/get
		String result = loadBalancedApi.get();

		assertEquals("Ribbon it's works!", result);
	}

	@SpringBootApplication
	public static class SampleSpringApplication {

		public static void main(String[] args) {
			SpringApplication.run(SampleSpringApplication.class, args);
		}
	}
}
