package com.github.ljtfreitas.restify.http.netflix.client.call.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.netflix.hystrix.HystrixCommand;

@RunWith(MockitoJUnitRunner.class)
public class HystrixEndpointCallExecutableAdapterTest {

	@Mock
	private EndpointCallExecutable<HystrixCommand<String>, String> hystrixCommandExecutable;

	@Mock
	private EndpointCall<String> call;

	private HystrixEndpointCallExecutableAdapter<String, String> adapter;

	private Object[] arguments;

	@Before
	public void setup() {
		arguments = new Object[0];

		when(call.execute())
			.thenReturn("call result");

		when(hystrixCommandExecutable.execute(call, arguments))
			.thenReturn(new SuccessHystrixCommand("call result"));

		adapter = new HystrixEndpointCallExecutableAdapter<>();
	}

	@Test
	public void shouldGetResultFromHystrixCommand() throws Exception {
		EndpointCallExecutable<String, String> executable = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker")), hystrixCommandExecutable);

		String result = executable.execute(call, arguments);

		assertEquals("call result", result);
	}

	@Test
	public void shouldGetFallbackWhenHystrixCommandFail() throws Exception {
		when(hystrixCommandExecutable.execute(call, arguments))
			.thenReturn(new FailHystrixCommand("fallback result"));

		EndpointCallExecutable<String, String> executable = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker")), hystrixCommandExecutable);

		String result = executable.execute(call, arguments);

		assertEquals("fallback result", result);
	}

	@Test
	public void shouldSupportsOnCircuitBreakerEndpointMethod() throws Exception {
		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker"));
		assertTrue(adapter.supports(endpointMethod));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodIsNotRunningOnOnCircuitBreaker() throws Exception {
		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("withoutCircuitBreaker"));
		assertFalse(adapter.supports(endpointMethod));
	}

	interface SomeType {

		@OnCircuitBreaker
		String onCircuitBreaker();

		String withoutCircuitBreaker();
	}
}
