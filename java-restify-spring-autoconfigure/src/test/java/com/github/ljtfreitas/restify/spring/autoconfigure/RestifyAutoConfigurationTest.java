package com.github.ljtfreitas.restify.spring.autoconfigure;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.ljtfreitas.restify.spring.autoconfigure.RestifyAutoConfigurationTest.TestRestifyConfiguration;
import com.github.ljtfreitas.restify.spring.autoconfigure.RestifyAutoConfigurationTest.TestRestifyConfiguration.TestRestifyConfigurationRegistrar;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestRestifyConfiguration.class, RestifyAutoConfiguration.class})
public class RestifyAutoConfigurationTest {

	@Autowired
	private ApplicationContext context;

	@Test
	public void shouldCreateBeanOfGitHubApiType() {
		assertNotNull(context.getBean(GitHubApi.class));
	}

	@Configuration
	@Import(TestRestifyConfigurationRegistrar.class)
	static class TestRestifyConfiguration {

		@Bean
		public RestTemplateBuilder restTemplateBuilder() {
			return new RestTemplateBuilder();
		}

		static class TestRestifyConfigurationRegistrar implements ImportBeanDefinitionRegistrar {
			@Override
			public void registerBeanDefinitions(AnnotationMetadata arg0, BeanDefinitionRegistry registry) {
				AutoConfigurationPackages.register(registry, RestifyAutoConfigurationTest.class.getPackage().getName());
			}
		}
	}
}
