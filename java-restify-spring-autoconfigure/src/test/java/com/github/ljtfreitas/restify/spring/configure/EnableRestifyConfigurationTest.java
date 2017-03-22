package com.github.ljtfreitas.restify.spring.configure;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.ljtfreitas.restify.spring.configure.EnableRestifyConfigurationTest.TestRestifyConfiguration;
import com.github.ljtfreitas.restify.spring.whatever.TwitterApi;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestRestifyConfiguration.class)
@DirtiesContext
public class EnableRestifyConfigurationTest {

	@Autowired
	private ApplicationContext context;

	@Test
	public void shouldCreateBeanOfTwitterApiType() {
		assertNotNull(context.getBean(TwitterApi.class));
	}

	@EnableRestify(packages = "com.github.ljtfreitas.restify.spring.whatever")
	static class TestRestifyConfiguration {
	}
}
