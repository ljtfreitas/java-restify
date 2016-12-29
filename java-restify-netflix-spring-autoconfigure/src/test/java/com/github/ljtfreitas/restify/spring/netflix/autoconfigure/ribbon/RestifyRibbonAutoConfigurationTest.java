package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.ribbon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.github.ljtfreitas.restify.spring.netflix.autoconfigure.ribbon.RestifyRibbonAutoConfigurationTest.SampleSpringApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleSpringApplication.class)
public class RestifyRibbonAutoConfigurationTest {

	@Autowired
	private ObjectProvider<LoadBalancedApi> provider;

	@Autowired @LoadBalanced
	private RestTemplate restTemplate;
	
	private MockRestServiceServer mockLoadBalancedApiServer;

	@Before
	public void setup() {
		mockLoadBalancedApiServer = MockRestServiceServer.createServer(restTemplate);

		// ribbon must resolve endpoint to http://localhost:8080/get!
		mockLoadBalancedApiServer.expect(requestTo("http://localhost:8080/get"))
			.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("Ribbon it's works!", MediaType.TEXT_PLAIN));
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
