package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.Fallback;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.WithFallback;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(MockitoJUnitRunner.class)
public class HystrixObservableCommandFallbackEndpointCallHandlerAdapterTest {

	@Mock
	private AsyncEndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncCall;

	private HystrixObservableCommandEndpointCallHandlerAdapter<String, String> adapter;

	@Spy
	private FallbackOfSameType fallback = new FallbackOfSameType();

	@Spy
	private FallbackOfOtherType fallbackOfOtherType = new FallbackOfOtherType();

	@Spy
	private FallbackWithEmptyObservable fallbackWithEmptyObservable = new FallbackWithEmptyObservable();

	@Before
	public void setup() {
		adapter = new HystrixObservableCommandEndpointCallHandlerAdapter<>(Fallback.of(fallback));

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldReturnFallbackValueWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(new RuntimeException("oooh!"));

		when(asyncCall.executeAsync())
			.thenReturn(future);

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, null);

		String result = hystrixCommand.observe().toBlocking().first();

		assertEquals("fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(fallback).command();
	}

	@Test
	public void shouldReturnSimpleFallbackValueWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("string")), delegate);

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, null);

		String result = hystrixCommand.observe().toBlocking().first();

		assertEquals("string fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(fallback).string();
	}

	@Test
	public void shouldReturnFutureFallbackValueWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("future")), delegate);

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(new RuntimeException("oooh!"));

		when(asyncCall.executeAsync())
			.thenReturn(future);

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, null);

		String result = hystrixCommand.observe().toBlocking().first();

		assertEquals("future fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(fallback).future();
	}

	@Test
	public void shouldReturnObservableFallbackValueWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, null);

		String result = hystrixCommand.observe().toBlocking().first();

		assertEquals("observable fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(fallback).observable();
	}

	@Test
	public void shouldReturnFallbackValueFromMethodWithArgsWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("args", String.class)), delegate);

		String arg = "whatever";

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, new Object[] {arg});

		String result = hystrixCommand.observe().toBlocking().first();

		assertEquals("fallback with arg " + arg, result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(fallback).args(arg);
	}

	@Test
	public void shouldReturnValueFromFallbackOfOtherTypeWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		adapter = new HystrixObservableCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, null);

		String result = hystrixCommand.observe().toBlocking().first();

		assertEquals("fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(fallbackOfOtherType).command();
	}

	@Test
	public void shouldReturnFutureValueFromFallbackOfOtherTypeWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		adapter = new HystrixObservableCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("future")), delegate);

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, null);

		String result = hystrixCommand.observe().toBlocking().first();

		assertEquals("future fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(fallbackOfOtherType).future();
	}

	@Test
	public void shouldReturnFallbackValueFromMethodWithArgsFromFallbackOfOtherTypeWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		adapter = new HystrixObservableCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("args", String.class)), delegate);

		String arg = "whatever";

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, new Object[] {arg});

		String result = hystrixCommand.observe().toBlocking().first();

		assertEquals("fallback with arg " + arg, result);
		assertEquals(delegate.returnType(), handler.returnType());

		verify(fallbackOfOtherType).args(arg);
	}

	@Test
	public void shouldReturnFallbackValueFromWithFallbackAnnotationWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		adapter = new HystrixObservableCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(OtherType.class.getMethod("command")), delegate);

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, null);

		String result = hystrixCommand.observe().toBlocking().first();

		assertEquals("fallback", result);
		assertEquals(delegate.returnType(), handler.returnType());
	}

	@Test
	public void shouldReturnEmptyObservableAsFallbackWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		adapter = new HystrixObservableCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackWithEmptyObservable));

		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, null);

		TestSubscriber<String> subscriber = new TestSubscriber<>();
		hystrixCommand.toObservable().subscribe(subscriber);

		subscriber.awaitTerminalEvent();
		assertThat(subscriber.getOnNextEvents(), empty());
	}

	@Test
	public void shouldReturnEmptyObservableWhenHystrixObservableCommandExecutionThrowExceptionWithNullAsFallbackValue() throws Exception {
		adapter = new HystrixObservableCommandEndpointCallHandlerAdapter<>(Fallback.of(fallbackWithEmptyObservable));

		AsyncEndpointCallHandler<HystrixObservableCommand<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("string")), delegate);

		HystrixObservableCommand<String> hystrixCommand = handler.handleAsync(asyncCall, null);

		TestSubscriber<String> subscriber = new TestSubscriber<>();
		hystrixCommand.toObservable().subscribe(subscriber);

		assertThat(subscriber.getOnNextEvents(), Matchers.empty());

		subscriber.awaitTerminalEvent();
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

	static class FallbackWithEmptyObservable {

		public Observable<String> observable() {
			return Observable.empty();
		}

		public String string() {
			return null;
		}
	}
}
