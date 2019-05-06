package com.github.ljtfreitas.restify.http.client.call.handler.reactor.circuitbreaker.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.Fallback;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class HystrixMonoFallbackEndpointCallHandlerAdapterTest {

	@Mock
	private AsyncEndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncCall;

	private HystrixMonoEndpointCallHandlerAdapter<String, String> adapter;

	@Spy
	private FallbackOfSameType fallback = new FallbackOfSameType();

	@Spy
	private FallbackOfOtherType fallbackOfOtherType = new FallbackOfOtherType();

	private RuntimeException exception;

	private CompletableFuture<String> exceptionAsFuture;

	@Before
	public void setup() {
		adapter = new HystrixMonoEndpointCallHandlerAdapter<>(Fallback.of(fallback));

		exception = new RuntimeException("oooh!");

		exceptionAsFuture = new CompletableFuture<>();
		exceptionAsFuture.completeExceptionally(exception);

		when(asyncCall.executeAsync())
			.thenReturn(exceptionAsFuture);
	}

	@Test
	public void shouldReturnFallbackValueOnMonoWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		AsyncEndpointCallHandler<Mono<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("mono")), delegate);

		Mono<String> mono = handler.handleAsync(asyncCall, null);

		StepVerifier.create(mono)
				.assertNext(result -> {
					assertEquals("mono fallback", result);
				})
				.verifyComplete();

		assertEquals(delegate.returnType(), handler.returnType());
		verify(fallback).mono();
	}

	@Test
	public void shouldReturnMonoFromFallbackMethodWithArgsWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		AsyncEndpointCallHandler<Mono<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("args", String.class)), delegate);

		String arg = "whatever";

		Mono<String> mono = handler.handleAsync(asyncCall, new Object[] {arg});

		StepVerifier.create(mono)
				.assertNext(result -> {
					assertEquals("fallback with arg " + arg, result);
				})
				.verifyComplete();

		assertEquals(delegate.returnType(), handler.returnType());
		verify(fallback).args(arg);
	}

	@Test
	public void shouldReturnMonoWithValueFromFallbackOfOtherTypeWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		adapter = new HystrixMonoEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		AsyncEndpointCallHandler<Mono<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("mono")), delegate);

		Mono<String> mono = handler.handleAsync(asyncCall, null);

		StepVerifier.create(mono)
				.verifyComplete();

		assertEquals(delegate.returnType(), handler.returnType());

		verify(fallbackOfOtherType).mono();
	}

	@Test
	public void shouldReturnMonoFromFallbackOfOtherTpeWithThrowableArgWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		adapter = new HystrixMonoEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		AsyncEndpointCallHandler<Mono<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("args", String.class)), delegate);

		String arg = "whatever";

		Mono<String> mono = handler.handleAsync(asyncCall, new Object[] {arg});

		StepVerifier.create(mono)
				.verifyErrorSatisfies(e -> {
					assertEquals(HystrixRuntimeException.class, e.getClass());
					assertSame(exception, e.getCause());
				});

		assertEquals(delegate.returnType(), handler.returnType());
		verify(fallbackOfOtherType).args(arg, exception);
	}

	@Test
	public void shouldReturnEmptyMonoFromFallbackOfOtherTypeWhenHystrixObservableCommandExecutionThrowExceptionWithNullAsFallbackValue() throws Exception {
		adapter = new HystrixMonoEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		AsyncEndpointCallHandler<Mono<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("mono")), delegate);

		Mono<String> mono = handler.handleAsync(asyncCall, null);

		StepVerifier.create(mono)
			.verifyComplete();
	}

	interface SomeType {

		Mono<String> mono();

		Mono<String> args(String arg);
	}

	private class FallbackOfSameType implements SomeType {

		@Override
		public Mono<String> mono() {
			return Mono.just("mono fallback");
		}

		@Override
		public Mono<String> args(String arg) {
			return Mono.just("fallback with arg " + arg);
		}
	}

	static class FallbackOfOtherType {

		Mono<String> mono() {
			return Mono.empty();
		}

		Mono<String> args(String arg, RuntimeException exception) {
			return Mono.error(exception);
		}
	}
}
