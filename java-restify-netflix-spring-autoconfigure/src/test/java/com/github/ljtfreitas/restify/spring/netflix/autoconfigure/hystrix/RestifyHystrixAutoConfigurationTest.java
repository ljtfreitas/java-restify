package com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.ljtfreitas.restify.spring.netflix.autoconfigure.hystrix.RestifyHystrixAutoConfigurationTest.SampleSpringApplication;
import com.netflix.hystrix.HystrixCommand;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleSpringApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT,
	properties = "restify.ribbon.enabled=false")
public class RestifyHystrixAutoConfigurationTest {

	@Autowired
	private BadApi badApi;

	@Autowired
	private GoodApi goodApi;

	@Test
	public void shouldGetFallbackToBadApiWhenOnCircuitBreakerMethodIsCalled() {
		String result = badApi.get(); // break (see BadController: response 500 status code) -> go to fallback...

		// see FallbackBadApi class
		assertEquals("this is BadApi fallback!", result);
	}

	@Test
	public void shouldGetFallbackToBadApiWhenHystrixCommandIsCalled() {
		HystrixCommand<String> command = badApi.getAsHystrixCommand();

		String result = command.execute(); // break (see BadController: response 500 status code) -> go to fallback...

		// see FallbackBadApi class
		assertEquals("this is BadApi (command) fallback!", result);
	}

	@Test
	public void shouldGetNormalResultOfGoodApiWhenOnCircuitBreakerMethodIsCalled() {
		String result = goodApi.get(); // (see GoodController: response 200 status code)

		assertEquals("It's works!", result);
	}

	@Test
	public void shouldGetNormalResultOfGoodApiWhenHystrixCommandIsCalled() {
		HystrixCommand<String> command = goodApi.getAsHystrixCommand();

		String result = command.execute(); // (see GoodController: response 200 status code)

		assertEquals("It's works!", result);
	}

	@SpringBootApplication
	public static class SampleSpringApplication {

		public static void main(String[] args) {
			SpringApplication.run(SampleSpringApplication.class, args);
		}
	}
}
