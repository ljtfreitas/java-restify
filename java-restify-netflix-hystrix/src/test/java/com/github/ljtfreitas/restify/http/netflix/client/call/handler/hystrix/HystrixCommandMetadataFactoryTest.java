package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_STRATEGY;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS;
import static com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty.THREAD_POOL_CORE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.CircuitBreakerProperty;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreaker;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadata;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreakerMetadataResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;

@RunWith(MockitoJUnitRunner.class)
public class HystrixCommandMetadataFactoryTest {

	@Mock
	private OnCircuitBreakerMetadataResolver onCircuitBreakerMetadataResolver;

	@Mock
	private OnCircuitBreakerMetadata onCircuitBreakerMetadata;

	@InjectMocks
	private HystrixCommandMetadataFactory factory;

	@Before
	public void setup() {
		when(onCircuitBreakerMetadataResolver.resolve(notNull(EndpointMethod.class)))
			.thenReturn(onCircuitBreakerMetadata);

		when(onCircuitBreakerMetadata.groupKey()).thenReturn(Optional.empty());
		when(onCircuitBreakerMetadata.commandKey()).thenReturn(Optional.empty());
		when(onCircuitBreakerMetadata.threadPoolKey()).thenReturn(Optional.empty());
		when(onCircuitBreakerMetadata.properties()).thenReturn(Collections.emptyMap());
	}

	@Test
	public void shouldBuildHystrixCommandKeysUsingMethodMetadata() throws Exception {
		HystrixCommand.Setter hystrixCommandProperties = factory
				.create(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("simple")))
					.asCommand();

		EmptyHystrixCommand command = new EmptyHystrixCommand(hystrixCommandProperties);

		assertEquals("MyApiWithCircuitBreaker", command.getCommandGroup().name());
		assertEquals("simple", command.getCommandKey().name());
		assertEquals("MyApiWithCircuitBreaker", command.getThreadPoolKey().name());
	}

	@Test
	public void shouldBuildHystrixObservableCommandKeysUsingMethodMetadata() throws Exception {
		HystrixObservableCommand.Setter hystrixCommandProperties = factory
				.create(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("simple")))
					.asObservableCommand();

		EmptyHystrixObservableCommand command = new EmptyHystrixObservableCommand(hystrixCommandProperties);

		assertEquals("MyApiWithCircuitBreaker", command.getCommandGroup().name());
		assertEquals("simple", command.getCommandKey().name());
		assertEquals("MyApiWithCircuitBreaker", command.getThreadPoolKey().name());
	}

	@Test
	public void shouldBuildHystrixCommandKeysUsingMethodAnnotation() throws Exception {
		when(onCircuitBreakerMetadata.groupKey()).thenReturn(Optional.of("myGroupKey"));
		when(onCircuitBreakerMetadata.commandKey()).thenReturn(Optional.of("myTestCommandKey"));
		when(onCircuitBreakerMetadata.threadPoolKey()).thenReturn(Optional.of("myTestThreadPoolKey"));

		HystrixCommand.Setter hystrixCommandProperties = factory
				.create(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("customizedKeys")))
					.asCommand();

		EmptyHystrixCommand command = new EmptyHystrixCommand(hystrixCommandProperties);

		assertEquals("myGroupKey", command.getCommandGroup().name());
		assertEquals("myTestCommandKey", command.getCommandKey().name());
		assertEquals("myTestThreadPoolKey", command.getThreadPoolKey().name());
	}

	@Test
	public void shouldBuildHystrixObservableCommandKeysUsingMethodAnnotation() throws Exception {
		HystrixObservableCommand.Setter hystrixCommandProperties = factory
				.create(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("customizedKeys")))
					.asObservableCommand();

		EmptyHystrixObservableCommand command = new EmptyHystrixObservableCommand(hystrixCommandProperties);

		assertEquals("myGroupKey", command.getCommandGroup().name());
		assertEquals("myTestCommandKey", command.getCommandKey().name());
		assertEquals("myGroupKey", command.getThreadPoolKey().name());
	}

	@Test
	public void shouldBuildHystrixCommandPropertiesUsingMethodAnnotation() throws Exception {
		Map<String, String> properties = new HashMap<>();
		properties.put(EXECUTION_ISOLATION_STRATEGY, "SEMAPHORE");
		properties.put(EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, "2500");
		properties.put(THREAD_POOL_CORE_SIZE, "50");
		
		when(onCircuitBreakerMetadata.properties()).thenReturn(properties);

		HystrixCommand.Setter hystrixCommandProperties = factory
				.create(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("customizedProperties")))
					.asCommand();

		EmptyHystrixCommand command = new EmptyHystrixCommand(hystrixCommandProperties);

		assertEquals("MyApiWithCircuitBreaker", command.getCommandGroup().name());
		assertEquals("customizedProperties", command.getCommandKey().name());
		assertEquals("MyApiWithCircuitBreaker", command.getThreadPoolKey().name());

		assertEquals(ExecutionIsolationStrategy.SEMAPHORE, command.getProperties().executionIsolationStrategy().get());
		assertEquals(Integer.valueOf(2500), command.getProperties().executionTimeoutInMilliseconds().get());
	}

	@Test
	public void shouldBuildHystrixObservableCommandPropertiesUsingMethodAnnotation() throws Exception {
		HystrixObservableCommand.Setter hystrixCommandProperties = factory
				.create(new SimpleEndpointMethod(MyApiWithCircuitBreaker.class.getMethod("customizedProperties")))
					.asObservableCommand();

		EmptyHystrixObservableCommand command = new EmptyHystrixObservableCommand(hystrixCommandProperties);

		assertEquals("MyApiWithCircuitBreaker", command.getCommandGroup().name());
		assertEquals("customizedProperties", command.getCommandKey().name());
		assertEquals("MyApiWithCircuitBreaker", command.getThreadPoolKey().name());

		assertEquals(ExecutionIsolationStrategy.SEMAPHORE, command.getProperties().executionIsolationStrategy().get());
		assertEquals(Integer.valueOf(2500), command.getProperties().executionTimeoutInMilliseconds().get());
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
}
