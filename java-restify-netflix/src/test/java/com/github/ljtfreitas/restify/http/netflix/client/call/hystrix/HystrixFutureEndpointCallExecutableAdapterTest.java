package com.github.ljtfreitas.restify.http.netflix.client.call.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;

@RunWith(MockitoJUnitRunner.class)
public class HystrixFutureEndpointCallExecutableAdapterTest {

	@Mock
	private EndpointCallExecutable<HystrixCommand<String>, String> hystrixCommandExecutable;

	@Mock
	private EndpointCall<String> call;

	private HystrixFutureEndpointCallExecutableAdapter<String, String> adapter;

	private Object[] arguments;

	@Before
	public void setup() {
		arguments = new Object[0];

		when(hystrixCommandExecutable.execute(call, arguments))
			.thenReturn(new SuccessHystrixCommand("call result"));

		adapter = new HystrixFutureEndpointCallExecutableAdapter<>();
	}

	@Test
	public void shouldGetFutureFromHystrixCommand() throws Exception {
		EndpointCallExecutable<Future<String>, String> executable = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("futureOnCircuitBreaker")), hystrixCommandExecutable);

		Future<String> future = executable.execute(call, arguments);

		assertEquals("call result", future.get());
	}

	@Test
	public void shouldGetFutureWithFallbackWhenHystrixCommandFail() throws Exception {
		when(hystrixCommandExecutable.execute(call, arguments))
			.thenReturn(new FailHystrixCommand("fallback result"));

		EndpointCallExecutable<Future<String>, String> executable = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("futureOnCircuitBreaker")), hystrixCommandExecutable);

		Future<String> future = executable.execute(call, arguments);

		assertEquals("fallback result", future.get());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodHasOnCircuitBreakerAnnotation() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("futureOnCircuitBreaker"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodHasNotOnCircuitBreakerAnnotation() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("futureWithoutCircuitBreaker"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNoFuture() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreakerOtherType"))));
	}

	@Test
	public void shouldReturnHystrixCommandAsReturnTypeWithFutureParameterizedType() throws Exception {
		JavaType returnType = adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("futureOnCircuitBreaker")));
		
		assertTrue(returnType.parameterized());
		
		ParameterizedType parameterizedReturnType = returnType.as(ParameterizedType.class);
	
		assertEquals(HystrixCommand.class, parameterizedReturnType.getRawType());
		assertEquals(String.class, parameterizedReturnType.getActualTypeArguments()[0]);
	}

	@Test
	public void shouldReturnHystrixCommandAsReturnTypeWithObjectTypeWhenFutureIsNotParameterized() throws Exception {
		JavaType returnType = adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFuture")));
		
		assertTrue(returnType.parameterized());
		
		ParameterizedType parameterizedReturnType = returnType.as(ParameterizedType.class);
	
		assertEquals(HystrixCommand.class, parameterizedReturnType.getRawType());
		assertEquals(Object.class, parameterizedReturnType.getActualTypeArguments()[0]);
	}

	interface SomeType {

		@OnCircuitBreaker
		Future<String> futureOnCircuitBreaker();

		@SuppressWarnings("rawtypes")
		Future dumbFuture();
		
		Future<String> futureWithoutCircuitBreaker();

		@OnCircuitBreaker
		String onCircuitBreakerOtherType();
	}
}
