package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix.RestifyHystrixAutoConfigurationTest.SampleSpringApplication;
import com.netflix.hystrix.HystrixCommand;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleSpringApplication.class, properties = "restify.ribbon.enabled=false")
public class RestifyHystrixAutoConfigurationTest {

	@Autowired
	private BadApi badApi;

	@Autowired
	private GoodApi goodApi;

	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer mockApiServer;

	@Before
	public void setup() {
		mockApiServer = MockRestServiceServer.createServer(restTemplate);
	}

	@Test
	public void shouldGetFallbackToBadApiWhenOnCircuitBreakerMethodIsCalled() {
		mockApiServer.expect(requestTo("http://localhost:8080/bad/get"))
			.andExpect(method(HttpMethod.GET))
				.andRespond(withServerError());

		String result = badApi.get(); // break (response is 500) -> go to fallback...

		// see FallbackBadApi class
		assertEquals("this is BadApi fallback!", result);
	}

	@Test
	public void shouldGetFallbackToBadApiWhenHystrixCommandIsCalled() {
		mockApiServer.expect(requestTo("http://localhost:8080/bad/get"))
    		.andExpect(method(HttpMethod.GET))
    			.andRespond(withServerError());

		HystrixCommand<String> command = badApi.getAsHystrixCommand();

		String result = command.execute(); // break (response is 500) -> go to fallback...

		// see FallbackBadApi class
		assertEquals("this is BadApi (command) fallback!", result);
	}

	@Test
	public void shouldGetNormalResultOfGoodApiWhenOnCircuitBreakerMethodIsCalled() {
		mockApiServer.expect(requestTo("http://localhost:8080/good/get"))
			.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("It's works!", MediaType.TEXT_PLAIN));

		String result = goodApi.get(); // response is 200

		assertEquals("It's works!", result);
	}

	@Test
	public void shouldGetNormalResultOfGoodApiWhenHystrixCommandIsCalled() {
		mockApiServer.expect(requestTo("http://localhost:8080/good/get"))
			.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess("It's works!", MediaType.TEXT_PLAIN));

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
