package com.github.ljtfreitas.restify.spring.configure;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.ljtfreitas.restify.spring.configure.EnableRestifyConfigurationTest.TestRestifyConfiguration;
import com.github.ljtfreitas.restify.spring.whatever.TwitterApi;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestRestifyConfiguration.class)
public class EnableRestifyConfigurationTest {

	@Autowired
	private ApplicationContext context;

	@Test
	public void shouldCreateBeanOfTwitterApiType() {
		assertNotNull(context.getBean(TwitterApi.class));
	}

	@SpringBootApplication
	@EnableRestify(packages = "com.github.ljtfreitas.restify.spring.whatever")
	static class TestRestifyConfiguration {
	}
}
