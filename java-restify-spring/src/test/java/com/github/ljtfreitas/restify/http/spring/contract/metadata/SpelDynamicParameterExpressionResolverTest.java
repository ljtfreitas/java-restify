package com.github.ljtfreitas.restify.http.spring.contract.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TestPropertySource("classpath:spring-restify.properties")
public class SpelDynamicParameterExpressionResolverTest {

	@Autowired
	private ConfigurableBeanFactory beanFactory;

	private SpelDynamicParameterExpressionResolver resolver;

	@Before
	public void setup() {
		resolver = new SpelDynamicParameterExpressionResolver(beanFactory);
	}

	@Test
	public void shouldEvaluateSpelExpressionOfProperties() {
		String endpoint = resolver.resolve("${api.endpoint}");
		assertEquals("http://my.api.com", endpoint.toString());
	}

	@Test
	public void shouldEvaluateSpelExpressionWithMultiplesProperties() {
		String endpoint = resolver.resolve("${api.endpoint}/path/${api.resource.path}");
		assertEquals("http://my.api.com/path/resource-path", endpoint.toString());
	}

	@Test
	public void shouldEvaluateSpelExpressionOfBeanProperty() {
		String endpoint = resolver.resolve("#{myApiBean.endpoint}");
		assertEquals("http://my.api.com", endpoint.toString());
	}

	@Test
	public void shouldEvaluateSimpleStringToSameValue() {
		String endpoint = resolver.resolve("http://my.api.com");
		assertEquals("http://my.api.com", endpoint.toString());
	}

	@Test
	public void shouldEvaluateDynamicParameterToSameValue() {
		String expression = resolver.resolve("{endpoint}");
		assertEquals("{endpoint}", expression.toString());
	}

	@Configuration
	static class SpelEndpointExpressionResolverConfiguration {

		@Bean
		MyApiBean myApiBean() {
			return new MyApiBean("http://my.api.com");
		}
	}

	static class MyApiBean {

		String endpoint;

		MyApiBean(String endpoint) {
			this.endpoint = endpoint;
		}

		public String getEndpoint() {
			return endpoint;
		}
	}
}
