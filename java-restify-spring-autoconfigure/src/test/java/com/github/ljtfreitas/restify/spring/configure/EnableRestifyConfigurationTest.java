package com.github.ljtfreitas.restify.spring.configure;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import com.github.ljtfreitas.restify.spring.whatever.TwitterApi;

public class EnableRestifyConfigurationTest {

	@Test
	public void shouldCreateBeanOfTwitterApiType() {
		ApplicationContextRunner contextRunner = new ApplicationContextRunner()
				.withUserConfiguration(TestRestifyConfiguration.class);

		contextRunner.run(context -> {
			assertNotNull(context.getBean(TwitterApi.class));
		});

	}

	@Configuration
	@EnableRestify(packages = "com.github.ljtfreitas.restify.spring.whatever")
	static class TestRestifyConfiguration {
	}
}
