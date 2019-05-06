package com.github.ljtfreitas.restify.http.client.call.handler.reactor.circuitbreaker.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.Fallback;
import com.github.ljtfreitas.restify.http.client.call.handler.circuitbreaker.OnCircuitBreaker;
import com.github.ljtfreitas.restify.reflection.JavaType;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class HystrixMonoEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> call;

	private HystrixMonoEndpointCallHandlerAdapter<String, String> adapter;

	private Object[] arguments;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		arguments = new Object[0];

		when(call.executeAsync())
			.thenReturn(CompletableFuture.completedFuture("call result"));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		adapter = new HystrixMonoEndpointCallHandlerAdapter<>();
	}

	@Test
	public void shouldCreateMonoFromHystrixCommand() throws Exception {
		AsyncEndpointCallHandler<Mono<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("monoOnCircuitBreaker")), delegate);

		Mono<String> mono = handler.handleAsync(call, arguments);

		StepVerifier.create(mono)
			.assertNext(value -> assertEquals("call result", value))
				.verifyComplete();
	}

	@Test
	public void shouldCreateMonoFromFallbackWhenHystrixCommandFail() throws Exception {
		CompletableFuture<String> exceptionAsFuture = new CompletableFuture<>();
		exceptionAsFuture.completeExceptionally(new RuntimeException("ooops"));

		when(call.executeAsync())
			.thenReturn(exceptionAsFuture);

		adapter = new HystrixMonoEndpointCallHandlerAdapter<>(Fallback.of(new SomeFallback()));

		AsyncEndpointCallHandler<Mono<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("monoOnCircuitBreaker")), delegate);

		Mono<String> mono = handler.handleAsync(call, arguments);

		StepVerifier.create(mono)
			.assertNext(value -> assertEquals("fallback result", value))
				.verifyComplete();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodHasOnCircuitBreakerAnnotation() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("monoOnCircuitBreaker"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodHasNotOnCircuitBreakerAnnotation() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("monoWithoutCircuitBreaker"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotMono() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreakerOtherType"))));
	}

	@Test
	public void shouldReturnParameterizedArgumentAsReturnTypeToParameterizedMono() throws Exception {
		JavaType returnType = adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("monoOnCircuitBreaker")));

		assertEquals(JavaType.of(String.class), returnType);
	}

	@Test
	public void shouldReturnObjectAsReturnTypeWithParameterizedMono() throws Exception {
		JavaType returnType = adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbMono")));

		assertEquals(JavaType.of(Object.class), returnType);
	}

	interface SomeType {

		@OnCircuitBreaker
		Mono<String> monoOnCircuitBreaker();

		@SuppressWarnings("rawtypes")
		Mono dumbMono();

		Mono<String> monoWithoutCircuitBreaker();

		@OnCircuitBreaker
		String onCircuitBreakerOtherType();
	}

	static class SomeFallback {

		Collection<String> monoOnCircuitBreaker() {
			return Collections.singleton("fallback result");
		}
	}
}
