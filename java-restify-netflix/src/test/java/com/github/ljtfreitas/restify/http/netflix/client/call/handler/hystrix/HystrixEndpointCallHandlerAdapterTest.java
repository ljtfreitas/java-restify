package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

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
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.netflix.hystrix.HystrixCommand;

@RunWith(MockitoJUnitRunner.class)
public class HystrixEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<HystrixCommand<String>, String> hystrixCommandHandler;

	@Mock
	private EndpointCall<String> call;

	private HystrixEndpointCallHandlerAdapter<String, String> adapter;

	private Object[] arguments;

	@Before
	public void setup() {
		arguments = new Object[0];

		when(call.execute())
			.thenReturn("call result");

		when(hystrixCommandHandler.handle(call, arguments))
			.thenReturn(new SuccessHystrixCommand("call result"));

		adapter = new HystrixEndpointCallHandlerAdapter<>();
	}

	@Test
	public void shouldGetResultFromHystrixCommand() throws Exception {
		EndpointCallHandler<String, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker")), hystrixCommandHandler);

		String result = handler.handle(call, arguments);

		assertEquals("call result", result);
	}

	@Test
	public void shouldGetFallbackWhenHystrixCommandFail() throws Exception {
		when(hystrixCommandHandler.handle(call, arguments))
			.thenReturn(new FailHystrixCommand("fallback result"));

		EndpointCallHandler<String, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreaker")), hystrixCommandHandler);

		String result = handler.handle(call, arguments);

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
