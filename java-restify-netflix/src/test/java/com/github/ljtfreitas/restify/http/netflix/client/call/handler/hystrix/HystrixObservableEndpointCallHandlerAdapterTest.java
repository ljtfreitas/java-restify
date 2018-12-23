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

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;

@RunWith(MockitoJUnitRunner.class)
public class HystrixObservableEndpointCallHandlerAdapterTest {

	@Mock
	private AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> delegate;

	@Mock
	private AsyncEndpointCall<String> call;

	private HystrixObservableEndpointCallHandlerAdapter<String, String> adapter;

	private Object[] arguments;
	
	@Before
	public void setup() {
		arguments = new Object[0];

		when(delegate.handle(call, arguments))
			.thenReturn(new SuccessHystrixObservableCommand());

		adapter = new HystrixObservableEndpointCallHandlerAdapter<>();
	}

	@Test
	public void shouldGetObservableFromHystrixCommand() throws Exception {
		AsyncEndpointCallHandler<Observable<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("observableOnCircuitBreaker")), delegate);

		Observable<String> observable = handler.handleAsync(call, arguments);

		assertEquals("call result", observable.toBlocking().first());
	}

	@Test
	public void shouldGetObservableWithFallbackWhenHystrixCommandFail() throws Exception {
		when(delegate.handle(same(call), any()))
			.thenReturn(new FailHystrixObservableCommand());

		AsyncEndpointCallHandler<Observable<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("observableOnCircuitBreaker")), delegate);

		Observable<String> observable = handler.handleAsync(call, arguments);

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
	public void shouldReturnHystrixObservableCommandAsReturnTypeWithObservableParameterizedType() throws Exception {
		JavaType returnType = adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("observableOnCircuitBreaker")));
		
		assertTrue(returnType.parameterized());
		
		ParameterizedType parameterizedReturnType = returnType.as(ParameterizedType.class);
	
		assertEquals(HystrixObservableCommand.class, parameterizedReturnType.getRawType());
		assertEquals(String.class, parameterizedReturnType.getActualTypeArguments()[0]);
	}

	@Test
	public void shouldReturnHystrixObservableCommandAsReturnTypeWithObjectTypeWhenObservableIsNotParameterized() throws Exception {
		JavaType returnType = adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbObservable")));
		
		assertTrue(returnType.parameterized());
		
		ParameterizedType parameterizedReturnType = returnType.as(ParameterizedType.class);
	
		assertEquals(HystrixObservableCommand.class, parameterizedReturnType.getRawType());
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
	
	private class SuccessHystrixObservableCommand extends HystrixObservableCommand<String> {

		private SuccessHystrixObservableCommand() {
			super(HystrixCommandGroupKey.Factory.asKey("success"));
		}

		@Override
		protected Observable<String> construct() {
			return Observable.just("call result");
		}
	}

	private class FailHystrixObservableCommand extends HystrixObservableCommand<String> {

		private FailHystrixObservableCommand() {
			super(HystrixCommandGroupKey.Factory.asKey("simple"));
		}

		@Override
		protected Observable<String> construct() {
			return Observable.error(new RuntimeException("ooops"));
		}

		@Override
		protected Observable<String> resumeWithFallback() {
			return Observable.just("fallback result");
		}
	}
}
