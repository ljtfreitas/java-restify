package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.ribbon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.ljtfreitas.restify.spring.netflix.autoconfigure.ribbon.RestifyRibbonAutoConfigurationTest.SampleSpringApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleSpringApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class RestifyRibbonAutoConfigurationTest {

	@Autowired
	private ObjectProvider<LoadBalancedApi> provider;

	@Test
	public void shouldCreateBeanOfLoadBalancedApiType() {
		LoadBalancedApi loadBalancedApi = provider.getIfAvailable();

		assertNotNull(loadBalancedApi);

		// request against http://localhost:8090/get (see SampleController class)
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
