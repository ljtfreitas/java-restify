package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

import java.lang.reflect.ParameterizedType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import rx.Observable;

@RunWith(MockitoJUnitRunner.class)
public class HystrixObservableEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<HystrixCommand<String>, String> hystrixCommandHandler;

	@Mock
	private EndpointCall<String> call;

	private HystrixObservableEndpointCallHandlerAdapter<String, String> adapter;

	private Object[] arguments;

	@Before
	public void setup() {
		arguments = new Object[0];

		when(hystrixCommandHandler.handle(call, arguments))
			.thenReturn(new SuccessHystrixCommand());

		adapter = new HystrixObservableEndpointCallHandlerAdapter<>();
	}

	@Test
	public void shouldGetObservableFromHystrixCommand() throws Exception {
		EndpointCallHandler<Observable<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("observableOnCircuitBreaker")), hystrixCommandHandler);

		Observable<String> observable = handler.handle(call, arguments);

		assertEquals("call result", observable.toBlocking().first());
	}

	@Test
	public void shouldGetObservableWithFallbackWhenHystrixCommandFail() throws Exception {
		when(hystrixCommandHandler.handle(same(call), any()))
			.thenReturn(new FailHystrixCommand());

		EndpointCallHandler<Observable<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("observableOnCircuitBreaker")), hystrixCommandHandler);

		Observable<String> observable = handler.handle(call, arguments);

		assertEquals("fallback result", observable.toBlocking().first());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodHasOnCircuitBreakerAnnotation() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("observableOnCircuitBreaker"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodHasNotOnCircuitBreakerAnnotation() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("observableWithoutCircuitBreaker"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotObservable() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreakerOtherType"))));
	}

	@Test
	public void shouldReturnHystrixCommandAsReturnTypeWithObservableParameterizedType() throws Exception {
		JavaType returnType = adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("observableOnCircuitBreaker")));

		assertTrue(returnType.parameterized());

		ParameterizedType parameterizedReturnType = returnType.as(ParameterizedType.class);

		assertEquals(HystrixCommand.class, parameterizedReturnType.getRawType());
		assertEquals(String.class, parameterizedReturnType.getActualTypeArguments()[0]);
	}

	@Test
	public void shouldReturnHystrixCommandAsReturnTypeWithObjectTypeWhenObservableIsNotParameterized() throws Exception {
		JavaType returnType = adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbObservable")));

		assertTrue(returnType.parameterized());

		ParameterizedType parameterizedReturnType = returnType.as(ParameterizedType.class);

		assertEquals(HystrixCommand.class, parameterizedReturnType.getRawType());
		assertEquals(Object.class, parameterizedReturnType.getActualTypeArguments()[0]);
	}

	interface SomeType {

		@OnCircuitBreaker
		Observable<String> observableOnCircuitBreaker();

		@SuppressWarnings("rawtypes")
		Observable dumbObservable();

		Observable<String> observableWithoutCircuitBreaker();

		@OnCircuitBreaker
		String onCircuitBreakerOtherType();
	}

	private class SuccessHystrixCommand extends HystrixCommand<String> {

		private SuccessHystrixCommand() {
			super(HystrixCommandGroupKey.Factory.asKey("success"));
		}

		@Override
		protected String run() throws Exception {
			return "call result";
		}
	}

	private class FailHystrixCommand extends HystrixCommand<String> {

		private FailHystrixCommand() {
			super(HystrixCommandGroupKey.Factory.asKey("simple"));
		}

		@Override
		protected String run() throws Exception {
			throw new RuntimeException("ooops");
		}

		@Override
		protected String getFallback() {
			return "fallback result";
		}
	}
}
