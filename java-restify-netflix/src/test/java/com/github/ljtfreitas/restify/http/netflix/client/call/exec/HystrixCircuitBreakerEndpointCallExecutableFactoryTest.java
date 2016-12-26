package com.github.ljtfreitas.restify.http.netflix.client.call.exec;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.OnCircuitBreaker;

@RunWith(MockitoJUnitRunner.class)
public class HystrixCircuitBreakerEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> endpointCallStringExecutable;

	@Mock
	private EndpointCallExecutable<Future<String>, String> endpointCallFutureExecutable;

	@Mock
	private EndpointCall<String> endpointCallString;

	private HystrixCircuitBreakerEndpointCallExecutableFactory<String, String> hystrixCircuitBreakerStringExecutableFactory;

	private HystrixCircuitBreakerEndpointCallExecutableFactory<Future<String>, String> hystrixCircuitBreakerFutureExecutableFactory;

	private Object[] arguments;

	@Before
	public void setup() {
		arguments = new Object[0];

		when(endpointCallString.execute())
			.thenReturn("call result");

		when(endpointCallStringExecutable.execute(any(), any()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		hystrixCircuitBreakerStringExecutableFactory = new HystrixCircuitBreakerEndpointCallExecutableFactory<>();

		hystrixCircuitBreakerFutureExecutableFactory = new HystrixCircuitBreakerEndpointCallExecutableFactory<>();
	}

	@Test
	public void shouldExecuteHystrixCommandUsingEndpointCall() throws Exception {

		EndpointCallExecutable<String, String> executable = hystrixCircuitBreakerStringExecutableFactory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker")), endpointCallStringExecutable);

		String result = executable.execute(endpointCallString, arguments);

		assertEquals("call result", result);
	}

	@Test
	public void shouldExecuteHystrixCommandOnOtherThread() throws Exception {
		EndpointCallExecutable<Future<String>, String> executable = hystrixCircuitBreakerFutureExecutableFactory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker")), endpointCallFutureExecutable);

		ExecutorService executor = Executors.newSingleThreadExecutor();

		when(endpointCallFutureExecutable.execute(any(), any()))
			.then(invocation -> executor.submit(() -> invocation.getArgumentAt(0, EndpointCall.class).execute()));

		Future<String> result = executable.execute(endpointCallString, arguments);

		assertEquals("call result", result.get());
	}

	@Test
	public void shouldSupportsOnCircuitBreakerEndpointMethod() throws Exception {
		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker"));
		assertTrue(hystrixCircuitBreakerStringExecutableFactory.supports(endpointMethod));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodIsNotRunningOnOnCircuitBreaker() throws Exception {
		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("withoutCircuitBreaker"));
		assertFalse(hystrixCircuitBreakerStringExecutableFactory.supports(endpointMethod));
	}

	interface SomeType {

		@OnCircuitBreaker
		String onCircuitBreaker();

		String withoutCircuitBreaker();
	}
}
