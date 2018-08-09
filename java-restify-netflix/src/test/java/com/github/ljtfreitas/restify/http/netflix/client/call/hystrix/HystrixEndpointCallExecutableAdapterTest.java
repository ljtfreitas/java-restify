package com.github.ljtfreitas.restify.http.netflix.client.call.hystrix;

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
import com.github.ljtfreitas.restify.http.netflix.client.call.hystrix.HystrixEndpointCallExecutableAdapter;
import com.github.ljtfreitas.restify.http.netflix.client.call.hystrix.OnCircuitBreaker;

@RunWith(MockitoJUnitRunner.class)
public class HystrixEndpointCallExecutableAdapterTest {

	@Mock
	private EndpointCallExecutable<String, String> endpointCallStringExecutable;

	@Mock
	private EndpointCallExecutable<Future<String>, String> endpointCallFutureExecutable;

	@Mock
	private EndpointCall<String> endpointCallString;

	private HystrixEndpointCallExecutableAdapter<String, String> hystrixCircuitBreakerStringExecutableAdapter;

	private HystrixEndpointCallExecutableAdapter<Future<String>, String> hystrixCircuitBreakerFutureExecutableAdapter;

	private Object[] arguments;

	@Before
	public void setup() {
		arguments = new Object[0];

		when(endpointCallString.execute())
			.thenReturn("call result");

		when(endpointCallStringExecutable.execute(any(), any()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		hystrixCircuitBreakerStringExecutableAdapter = new HystrixEndpointCallExecutableAdapter<>();

		hystrixCircuitBreakerFutureExecutableAdapter = new HystrixEndpointCallExecutableAdapter<>();
	}

	@Test
	public void shouldExecuteHystrixCommandUsingEndpointCall() throws Exception {

		EndpointCallExecutable<String, String> executable = hystrixCircuitBreakerStringExecutableAdapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker")), endpointCallStringExecutable);

		String result = executable.execute(endpointCallString, arguments);

		assertEquals("call result", result);
	}

	@Test
	public void shouldExecuteHystrixCommandOnOtherThread() throws Exception {
		EndpointCallExecutable<Future<String>, String> executable = hystrixCircuitBreakerFutureExecutableAdapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker")), endpointCallFutureExecutable);

		ExecutorService executor = Executors.newSingleThreadExecutor();

		when(endpointCallFutureExecutable.execute(any(), any()))
			.then(invocation -> executor.submit(() -> invocation.getArgumentAt(0, EndpointCall.class).execute()));

		Future<String> result = executable.execute(endpointCallString, arguments);

		assertEquals("call result", result.get());
	}

	@Test
	public void shouldSupportsOnCircuitBreakerEndpointMethod() throws Exception {
		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker"));
		assertTrue(hystrixCircuitBreakerStringExecutableAdapter.supports(endpointMethod));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodIsNotRunningOnOnCircuitBreaker() throws Exception {
		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("withoutCircuitBreaker"));
		assertFalse(hystrixCircuitBreakerStringExecutableAdapter.supports(endpointMethod));
	}

	interface SomeType {

		@OnCircuitBreaker
		String onCircuitBreaker();

		String withoutCircuitBreaker();
	}
}
