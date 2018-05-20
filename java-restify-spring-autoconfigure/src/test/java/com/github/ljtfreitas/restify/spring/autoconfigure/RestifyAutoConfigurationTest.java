package com.github.ljtfreitas.restify.spring.autoconfigure;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import com.github.ljtfreitas.restify.spring.autoconfigure.RestifyAutoConfigurationTest.TestRestifyConfiguration.TestRestifyConfigurationRegistrar;

public class RestifyAutoConfigurationTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8080);

	private MockServerClient mockServerClient;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8080);

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/sample"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "text/plain")
					.withBody("It's works!"));
	}

	@Test
	public void shouldCreateBeanOfMyApiType() {
		ApplicationContextRunner contextRunner = new ApplicationContextRunner()
				.withUserConfiguration(TestRestifyConfiguration.class)
				.withConfiguration(AutoConfigurations.of(RestifyAutoConfiguration.class))
				.withPropertyValues("restify.my-api.endpoint:http://localhost:8080");

		contextRunner.run(context -> {
			MyApi myApi = context.getBean(MyApi.class);

			assertEquals("It's works!", myApi.sample());
		});
	}

	@Configuration
	@Import(TestRestifyConfigurationRegistrar.class)
	static class TestRestifyConfiguration {

		static class TestRestifyConfigurationRegistrar implements ImportBeanDefinitionRegistrar {
			@Override
			public void registerBeanDefinitions(AnnotationMetadata arg0, BeanDefinitionRegistry registry) {
				AutoConfigurationPackages.register(registry, RestifyAutoConfigurationTest.class.getPackage().getName());
			}
		}
	}
}
