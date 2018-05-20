package com.github.ljtfreitas.restify.spring.configure;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import com.github.ljtfreitas.restify.spring.whatever.WhateverApi;

public class EnableRestifyConfigurationTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8080);

	private MockServerClient mockServerClient;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8080);

		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/whatever"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "text/plain")
					.withBody("It's works!"));
	}

	@Test
	public void shouldCreateBeanOfWhateverApiType() {
		ApplicationContextRunner contextRunner = new ApplicationContextRunner()
				.withUserConfiguration(TestRestifyConfiguration.class);

		contextRunner.run(context -> {
			WhateverApi whateverApi = context.getBean(WhateverApi.class);

			assertEquals("It's works!", whateverApi.sample());
		});
	}

	@Configuration
	@EnableRestify(packages = "com.github.ljtfreitas.restify.spring.whatever")
	static class TestRestifyConfiguration {
	}
}
