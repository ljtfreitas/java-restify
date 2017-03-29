package com.github.ljtfreitas.restify.http.netflix.client.call.exec;

import static com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_CORE_SIZE;
import static com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_STRATEGY;
import static com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.CircuitBreakerProperty;
import com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.OnCircuitBreaker;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;

public class HystrixCommandMetadataFactoryTest {

	private HystrixCommandMetadataFactory factory;

	@Test
	public void shouldBuildHystrixCommandKeysUsingMethodMetadata() throws Exception {
		factory = new HystrixCommandMetadataFactory(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("simple")));

		HystrixCommand.Setter hystrixMetadata = factory.create();

		EmptyHystrixCommand command = new EmptyHystrixCommand(hystrixMetadata);

		assertEquals("MyApiWithCircuitBreaker", command.getCommandGroup().name());
		assertEquals("simple", command.getCommandKey().name());
		assertEquals("MyApiWithCircuitBreaker", command.getThreadPoolKey().name());
	}

	@Test
	public void shouldBuildHystrixCommandKeysUsingMethodAnnotation() throws Exception {
		factory = new HystrixCommandMetadataFactory(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("customizedKeys")));

		HystrixCommand.Setter hystrixMetadata = factory.create();

		EmptyHystrixCommand command = new EmptyHystrixCommand(hystrixMetadata);

		assertEquals("myGroupKey", command.getCommandGroup().name());
		assertEquals("myTestCommandKey", command.getCommandKey().name());
		assertEquals("myTestThreadPoolKey", command.getThreadPoolKey().name());
	}

	@Test
	public void shouldBuildHystrixCommandPropertiesUsingMethodAnnotation() throws Exception {
		factory = new HystrixCommandMetadataFactory(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("customizedProperties")));

		HystrixCommand.Setter hystrixMetadata = factory.create();

		EmptyHystrixCommand command = new EmptyHystrixCommand(hystrixMetadata);

		assertEquals("MyApiWithCircuitBreaker", command.getCommandGroup().name());
		assertEquals("customizedProperties", command.getCommandKey().name());
		assertEquals("MyApiWithCircuitBreaker", command.getThreadPoolKey().name());

		assertEquals(ExecutionIsolationStrategy.SEMAPHORE, command.getProperties().executionIsolationStrategy().get());
		assertEquals(Integer.valueOf(2500), command.getProperties().executionTimeoutInMilliseconds().get());
	}

	private class EmptyHystrixCommand extends HystrixCommand<String> {

		protected EmptyHystrixCommand(HystrixCommand.Setter setter) {
			super(setter);
		}

		@Override
		protected String run() throws Exception {
			return "empty";
		}
	}

	interface MyApiWithCircuitBreaker {

		@OnCircuitBreaker
		String simple();

		@OnCircuitBreaker(groupKey = "myGroupKey", commandKey = "myTestCommandKey", threadPoolKey = "myTestThreadPoolKey")
		String customizedKeys();

		@OnCircuitBreaker(properties = {
				@CircuitBreakerProperty(name = EXECUTION_ISOLATION_STRATEGY, value = "SEMAPHORE"),
				@CircuitBreakerProperty(name = EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, value = "2500"),
				@CircuitBreakerProperty(name = THREAD_POOL_CORE_SIZE, value = "50")})
		String customizedProperties();
	}
}
