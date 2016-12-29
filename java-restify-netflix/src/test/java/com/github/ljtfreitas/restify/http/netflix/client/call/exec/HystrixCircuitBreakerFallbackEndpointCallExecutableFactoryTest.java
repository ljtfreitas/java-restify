package com.github.ljtfreitas.restify.http.netflix.client.call.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.netflix.client.request.circuitbreaker.OnCircuitBreaker;

@RunWith(MockitoJUnitRunner.class)
public class HystrixCircuitBreakerFallbackEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> endpointCallStringExecutable;

	@Mock
	private EndpointCallExecutable<Future<String>, String> endpointCallFutureExecutable;

	@Mock
	private EndpointCall<String> endpointCallString;

	private HystrixCircuitBreakerFallbackEndpointCallExecutableFactory<String, String, SomeType> hystrixCircuitBreakerStringExecutableFactory;

	private HystrixCircuitBreakerFallbackEndpointCallExecutableFactory<Future<String>, String, SomeType> hystrixCircuitBreakerFutureExecutableFactory;

	private Object[] arguments;

	@Spy
	private SomeFallback fallback = new SomeFallback();

	@Before
	public void setup() {
		arguments = new Object[0];

		when(endpointCallString.execute())
			.thenReturn("call result");

		when(endpointCallStringExecutable.execute(any(), any()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		hystrixCircuitBreakerStringExecutableFactory = new HystrixCircuitBreakerFallbackEndpointCallExecutableFactory<>(fallback);

		hystrixCircuitBreakerFutureExecutableFactory = new HystrixCircuitBreakerFallbackEndpointCallExecutableFactory<>(fallback);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldExecuteHystrixCommandUsingEndpointCall() throws Exception {
		EndpointCallExecutable<String, String> executable = hystrixCircuitBreakerStringExecutableFactory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker")), endpointCallStringExecutable);

		String result = executable.execute(endpointCallString, arguments);

		assertEquals("call result", result);

		verify(endpointCallStringExecutable)
			.execute(notNull(EndpointCall.class), anyVararg());

		verify(endpointCallString).execute();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldExecuteHystrixCommandOnOtherThread() throws Exception {
		EndpointCallExecutable<Future<String>, String> executable = hystrixCircuitBreakerFutureExecutableFactory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker")), endpointCallFutureExecutable);

		ExecutorService executor = Executors.newSingleThreadExecutor();

		when(endpointCallFutureExecutable.execute(any(), any()))
			.then(invocation -> executor.submit(() -> invocation.getArgumentAt(0, EndpointCall.class).execute()));

		Future<String> result = executable.execute(endpointCallString, arguments);

		assertEquals("call result", result.get());

		verify(endpointCallFutureExecutable)
			.execute(notNull(EndpointCall.class), anyVararg());

		verify(endpointCallString).execute();
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

	private class SomeFallback implements SomeType {

		@Override
		public String onCircuitBreaker() {
			return "onCircuitBreaker";
		}

		@Override
		public String withoutCircuitBreaker() {
			return "withoutCircuitBreaker";
		}
	}
}
