package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix.RestifyHystrixAutoConfigurationTest.SampleSpringApplication;
import com.netflix.hystrix.HystrixCommand;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleSpringApplication.class)
public class RestifyHystrixAutoConfigurationTest {

	@Autowired
	private BadApi badApi;

	@Autowired
	private GoodApi goodApi;

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8080);

	private MockServerClient mockServerClient;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8080);
	}

	@Test
	public void shouldGetFallbackToBadApiWhenOnCircuitBreakerMethodIsCalled() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/bad"))
			.respond(response()
					.withStatusCode(500));

		String result = badApi.get(); // break (response is 500) -> go to fallback...

		// see FallbackBadApi class
		assertEquals("this is BadApi fallback!", result);
	}

	@Test
	public void shouldGetFallbackToBadApiWhenHystrixCommandIsCalled() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/bad"))
			.respond(response()
					.withStatusCode(500));

		HystrixCommand<String> command = badApi.getAsHystrixCommand();

		String result = command.execute(); // break (response is 500) -> go to fallback...

		// see FallbackBadApi class
		assertEquals("this is BadApi (command) fallback!", result);
	}

	@Test
	public void shouldGetNormalResultOfGoodApiWhenOnCircuitBreakerMethodIsCalled() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/good"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "text/plain")
					.withBody("It's works!"));

		String result = goodApi.get(); // response is 200

		assertEquals("It's works!", result);
	}

	@Test
	public void shouldGetNormalResultOfGoodApiWhenHystrixCommandIsCalled() {
		mockServerClient
			.when(request()
					.withMethod("GET")
					.withPath("/good"))
			.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "text/plain")
					.withBody("It's works!"));

		HystrixCommand<String> command = goodApi.getAsHystrixCommand();

		String result = command.execute(); // response is 200

		assertEquals("It's works!", result);
	}

	@SpringBootApplication
	public static class SampleSpringApplication {

		@Bean
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}

		public static void main(String[] args) {
			SpringApplication.run(SampleSpringApplication.class, args);
		}
	}
}
