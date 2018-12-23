package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import rx.Observable;

@RunWith(MockitoJUnitRunner.class)
public class HystrixCommandFallbackEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	private HystrixCommandEndpointCallHandlerAdapter<String, String> adapter;

	@Spy
	private FallbackOfSameType fallback = new FallbackOfSameType();

	@Spy
	private FallbackOfOtherType fallbackOfOtherType = new FallbackOfOtherType();

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(new DefaultFallback(fallback));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw new RuntimeException("oooh!");}, null);

		String result = hystrixCommand.execute();

		assertEquals("fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).command();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnSimpleFallbackValueWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("string")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw new RuntimeException("oooh!");}, null);

		String result = hystrixCommand.execute();

		assertEquals("string fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).string();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFutureFallbackValueWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("future")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw new RuntimeException("oooh!");}, null);

		String result = hystrixCommand.execute();

		assertEquals("future fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).future();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnObservableFallbackValueWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw new RuntimeException("oooh!");}, null);

		String result = hystrixCommand.execute();

		assertEquals("observable fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).observable();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueFromMethodWithArgsWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("args", String.class)), delegate);

		String arg = "whatever";

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw new RuntimeException("oooh!");}, new Object[] {arg});

		String result = hystrixCommand.execute();

		assertEquals("fallback with arg " + arg, result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallback).args(arg);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnValueFromFallbackOfOtherTypeWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(new DefaultFallback(fallbackOfOtherType));

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw new RuntimeException("oooh!");}, null);

		String result = hystrixCommand.execute();

		assertEquals("fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallbackOfOtherType).command();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFutureValueFromFallbackOfOtherTypeWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(new DefaultFallback(fallbackOfOtherType));

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("future")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw new RuntimeException("oooh!");}, null);

		String result = hystrixCommand.execute();

		assertEquals("future fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallbackOfOtherType).future();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueFromMethodWithArgsFromFallbackOfOtherTypeWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>(new DefaultFallback(fallbackOfOtherType));

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("args", String.class)), delegate);

		String arg = "whatever";

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw new RuntimeException("oooh!");}, new Object[] {arg});

		String result = hystrixCommand.execute();

		assertEquals("fallback with arg " + arg, result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
		verify(fallbackOfOtherType).args(arg);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueFromWithFallbackAnnotationWhenHystrixCommandExecutionThrowException() throws Exception {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>();

		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(OtherType.class.getMethod("command")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw new RuntimeException("oooh!");}, null);

		String result = hystrixCommand.execute();

		assertEquals("fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
	}

	interface SomeType {

		HystrixCommand<String> command();

		@SuppressWarnings("rawtypes")
		HystrixCommand dumbCommand();

		String string();

		Future<String> future();

		Observable<String> observable();

		String args(String arg);
	}

	@WithFallback(FallbackOfOtherType.class)
	interface OtherType {

		String command();
	}

	private class FallbackOfSameType implements SomeType {

		@Override
		public HystrixCommand<String> command() {
			return new HystrixCommand<String>(HystrixCommandGroupKey.Factory.asKey("fallback")) {
				@Override
				protected String run() throws Exception {
					return "fallback";
				}
			};
		}

		@SuppressWarnings("rawtypes")
		@Override
		public HystrixCommand dumbCommand() {
			return new HystrixCommand(HystrixCommandGroupKey.Factory.asKey("fallback")) {
				@Override
				protected Object run() throws Exception {
					return "fallback";
				}
			};
		}

		@Override
		public String string() {
			return "string fallback";
		}

		@Override
		public Future<String> future() {
			return CompletableFuture.completedFuture("future fallback");
		}

		@Override
		public Observable<String> observable() {
			return Observable.just("observable fallback");
		}

		@Override
		public String args(String arg) {
			return "fallback with arg " + arg;
		}
	}

	static class FallbackOfOtherType {

		public String command() {
			return "fallback";
		}

		public String future() {
			return "future fallback";
		}

		public String args(String arg) {
			return "fallback with arg " + arg;
		}
	}
}
