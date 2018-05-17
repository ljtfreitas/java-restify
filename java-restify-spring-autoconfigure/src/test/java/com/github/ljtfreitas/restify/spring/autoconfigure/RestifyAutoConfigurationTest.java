package com.github.ljtfreitas.restify.spring.autoconfigure;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
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

	@Test
	public void shouldCreateBeanOfGitHubApiType() {
		ApplicationContextRunner contextRunner = new ApplicationContextRunner()
				.withUserConfiguration(TestRestifyConfiguration.class)
				.withConfiguration(AutoConfigurations.of(RestifyAutoConfiguration.class))
				.withPropertyValues("restify.github.endpoint:http://api.github.com");

		contextRunner.run(context -> {
			assertNotNull(context.getBean(GitHubApi.class));
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
