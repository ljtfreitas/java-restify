package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_STRATEGY;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_CORE_SIZE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreaker;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;

public class HystrixCommandMetadataFactoryTest {

	private HystrixCommandMetadataFactory factory;

	@Test
	public void shouldBuildHystrixCommandKeysUsingMethodMetadata() throws Exception {
		factory = new HystrixCommandMetadataFactory(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("simple")));

		HystrixCommand.Setter hystrixCommandProperties = factory.create().asCommand();

		EmptyHystrixCommand command = new EmptyHystrixCommand(hystrixCommandProperties);

		assertEquals("MyApiWithCircuitBreaker", command.getCommandGroup().name());
		assertEquals("simple", command.getCommandKey().name());
		assertEquals("MyApiWithCircuitBreaker", command.getThreadPoolKey().name());
	}

	@Test
	public void shouldBuildHystrixObservableCommandKeysUsingMethodMetadata() throws Exception {
		factory = new HystrixCommandMetadataFactory(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("simple")));

		HystrixObservableCommand.Setter hystrixCommandProperties = factory.create().asObservableCommand();

		EmptyHystrixObservableCommand command = new EmptyHystrixObservableCommand(hystrixCommandProperties);

		assertEquals("MyApiWithCircuitBreaker", command.getCommandGroup().name());
		assertEquals("simple", command.getCommandKey().name());
		assertEquals("MyApiWithCircuitBreaker", command.getThreadPoolKey().name());
	}

	@Test
	public void shouldBuildHystrixCommandKeysUsingMethodAnnotation() throws Exception {
		factory = new HystrixCommandMetadataFactory(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("customizedKeys")));

		HystrixCommand.Setter hystrixCommandProperties = factory.create().asCommand();

		EmptyHystrixCommand command = new EmptyHystrixCommand(hystrixCommandProperties);

		assertEquals("myGroupKey", command.getCommandGroup().name());
		assertEquals("myTestCommandKey", command.getCommandKey().name());
		assertEquals("myTestThreadPoolKey", command.getThreadPoolKey().name());
	}

	@Test
	public void shouldBuildHystrixObservableCommandKeysUsingMethodAnnotation() throws Exception {
		factory = new HystrixCommandMetadataFactory(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("customizedKeys")));

		HystrixObservableCommand.Setter hystrixCommandProperties = factory.create().asObservableCommand();

		EmptyHystrixObservableCommand command = new EmptyHystrixObservableCommand(hystrixCommandProperties);

		assertEquals("myGroupKey", command.getCommandGroup().name());
		assertEquals("myTestCommandKey", command.getCommandKey().name());
		assertEquals("myGroupKey", command.getThreadPoolKey().name());
	}

	@Test
	public void shouldBuildHystrixCommandPropertiesUsingMethodAnnotation() throws Exception {
		factory = new HystrixCommandMetadataFactory(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("customizedProperties")));

		HystrixCommand.Setter hystrixCommandProperties = factory.create().asCommand();

		EmptyHystrixCommand command = new EmptyHystrixCommand(hystrixCommandProperties);

		assertEquals("MyApiWithCircuitBreaker", command.getCommandGroup().name());
		assertEquals("customizedProperties", command.getCommandKey().name());
		assertEquals("MyApiWithCircuitBreaker", command.getThreadPoolKey().name());

		assertEquals(ExecutionIsolationStrategy.SEMAPHORE, command.getProperties().executionIsolationStrategy().get());
		assertEquals(Integer.valueOf(2500), command.getProperties().executionTimeoutInMilliseconds().get());
	}

	@Test
	public void shouldBuildHystrixObservableCommandPropertiesUsingMethodAnnotation() throws Exception {
		factory = new HystrixCommandMetadataFactory(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("customizedProperties")));

		HystrixObservableCommand.Setter hystrixCommandProperties = factory.create().asObservableCommand();

		EmptyHystrixObservableCommand command = new EmptyHystrixObservableCommand(hystrixCommandProperties);

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

	private class EmptyHystrixObservableCommand extends HystrixObservableCommand<String> {

		protected EmptyHystrixObservableCommand(HystrixObservableCommand.Setter setter) {
			super(setter);
		}

		@Override
		protected Observable<String> construct() {
			return Observable.just("empty");
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
