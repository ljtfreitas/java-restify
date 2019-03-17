package com.github.ljtfreitas.restify.http.client.call.handler.reactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
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

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class HystrixFluxFallbackEndpointCallHandlerAdapterTest {

	@Mock
	private AsyncEndpointCallHandler<Collection<String>, Collection<String>> delegate;

	@Mock
	private AsyncEndpointCall<Collection<String>> asyncCall;

	private HystrixFluxEndpointCallHandlerAdapter<String, Collection<String>> adapter;

	@Spy
	private FallbackOfSameType fallback = new FallbackOfSameType();

	@Spy
	private FallbackOfOtherType fallbackOfOtherType = new FallbackOfOtherType();

	private RuntimeException exception;

	private CompletableFuture<Collection<String>> exceptionAsFuture;

	@Before
	public void setup() {
		adapter = new HystrixFluxEndpointCallHandlerAdapter<>(Fallback.of(fallback));

		exception = new RuntimeException("oooh!");

		exceptionAsFuture = new CompletableFuture<>();
		exceptionAsFuture.completeExceptionally(exception);

		when(asyncCall.executeAsync())
			.thenReturn(exceptionAsFuture);
	}

	@Test
	public void shouldReturnFallbackValueOnFluxWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		AsyncEndpointCallHandler<Flux<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("flux")), delegate);

		Flux<String> flux = handler.handleAsync(asyncCall, null);

		StepVerifier.create(flux)
				.assertNext(result -> {
					assertEquals("flux fallback", result);
				})
				.assertNext(result -> {
					assertEquals("more one value...", result);
				})
				.verifyComplete();

		assertEquals(delegate.returnType(), handler.returnType());
		verify(fallback).flux();
	}

	@Test
	public void shouldReturnFluxFromFallbackMethodWithArgsWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		AsyncEndpointCallHandler<Flux<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("args", String.class)), delegate);

		String arg = "whatever";

		Flux<String> flux = handler.handleAsync(asyncCall, new Object[] {arg});

		StepVerifier.create(flux)
				.assertNext(result -> {
					assertEquals("fallback with arg " + arg, result);
				})
				.verifyComplete();

		assertEquals(delegate.returnType(), handler.returnType());
		verify(fallback).args(arg);
	}

	@Test
	public void shouldReturnFluxWithValueFromFallbackOfOtherTypeWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		adapter = new HystrixFluxEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		AsyncEndpointCallHandler<Flux<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("flux")), delegate);

		Flux<String> flux = handler.handleAsync(asyncCall, null);

		StepVerifier.create(flux)
				.verifyComplete();

		assertEquals(delegate.returnType(), handler.returnType());

		verify(fallbackOfOtherType).flux();
	}

	@Test
	public void shouldReturnFluxFromFallbackOfOtherTpeWithThrowableArgWhenHystrixObservableCommandExecutionThrowException() throws Exception {
		adapter = new HystrixFluxEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		AsyncEndpointCallHandler<Flux<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("args", String.class)), delegate);

		String arg = "whatever";

		Flux<String> flux = handler.handleAsync(asyncCall, new Object[] {arg});

		StepVerifier.create(flux)
				.assertNext(result -> {
					assertEquals("fallback with arg " + arg, result);
				})
				.verifyErrorSatisfies(e -> {
					assertEquals(HystrixRuntimeException.class, e.getClass());
					assertSame(exception, e.getCause());
				});

		assertEquals(delegate.returnType(), handler.returnType());
		verify(fallbackOfOtherType).args(arg, exception);
	}

	@Test
	public void shouldReturnEmptyFluxFromFallbackOfOtherTypeWhenHystrixObservableCommandExecutionThrowExceptionWithNullAsFallbackValue() throws Exception {
		adapter = new HystrixFluxEndpointCallHandlerAdapter<>(Fallback.of(fallbackOfOtherType));

		AsyncEndpointCallHandler<Flux<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("flux")), delegate);

		Flux<String> flux = handler.handleAsync(asyncCall, null);

		StepVerifier.create(flux)
			.verifyComplete();
	}

	interface SomeType {

		Flux<String> flux();

		Flux<String> args(String arg);
	}

	private class FallbackOfSameType implements SomeType {

		@Override
		public Flux<String> flux() {
			return Flux.just("flux fallback", "more one value...");
		}

		@Override
		public Flux<String> args(String arg) {
			return Flux.just("fallback with arg " + arg);
		}
	}

	static class FallbackOfOtherType {

		Flux<String> flux() {
			return Flux.empty();
		}

		Flux<String> args(String arg, RuntimeException exception) {
			return Flux.concat(Flux.just("fallback with arg " + arg), Flux.error(exception));
		}
	}
}
