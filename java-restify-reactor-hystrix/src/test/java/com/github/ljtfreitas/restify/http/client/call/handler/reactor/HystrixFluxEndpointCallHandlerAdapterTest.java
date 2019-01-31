package com.github.ljtfreitas.restify.http.client.call.handler.reactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
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

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class HystrixFluxEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<Collection<String>, Collection<String>> delegate;

	@Mock
	private AsyncEndpointCall<Collection<String>> call;

	private HystrixFluxEndpointCallHandlerAdapter<String, Collection<String>> adapter;

	private Object[] arguments;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		arguments = new Object[0];

		when(call.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(Arrays.asList("call result")));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		adapter = new HystrixFluxEndpointCallHandlerAdapter<>();
	}

	@Test
	public void shouldCreateFluxFromHystrixCommand() throws Exception {
		AsyncEndpointCallHandler<Flux<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("fluxOnCircuitBreaker")), delegate);

		Flux<String> flux = handler.handleAsync(call, arguments);

		StepVerifier.create(flux)
			.assertNext(value -> assertEquals("call result", value))
				.verifyComplete();
	}

	@Test
	public void shouldCreateFluxFromFallbackWhenHystrixCommandFail() throws Exception {
		CompletableFuture<Collection<String>> exceptionAsFuture = new CompletableFuture<>();
		exceptionAsFuture.completeExceptionally(new RuntimeException("ooops"));

		when(call.executeAsync())
			.thenReturn(exceptionAsFuture);

		adapter = new HystrixFluxEndpointCallHandlerAdapter<>(Fallback.of(new SomeFallback()));

		AsyncEndpointCallHandler<Flux<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("fluxOnCircuitBreaker")), delegate);

		Flux<String> flux = handler.handleAsync(call, arguments);

		StepVerifier.create(flux)
			.assertNext(value -> assertEquals("fallback result", value))
				.verifyComplete();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodHasOnCircuitBreakerAnnotation() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("fluxOnCircuitBreaker"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodHasNotOnCircuitBreakerAnnotation() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("fluxWithoutCircuitBreaker"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotFlux() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("onCircuitBreakerOtherType"))));
	}

	@Test
	public void shouldReturnParameterizedArgumentAsReturnTypeToParameterizedFlux() throws Exception {
		JavaType returnType = adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("fluxOnCircuitBreaker")));

		assertTrue(returnType.parameterized());

		ParameterizedType parameterizedType = returnType.as(ParameterizedType.class);

		assertEquals(Collection.class, parameterizedType.getRawType());
		assertEquals(String.class, parameterizedType.getActualTypeArguments()[0]);
	}

	@Test
	public void shouldReturnObjectAsReturnTypeWithParameterizedFlux() throws Exception {
		JavaType returnType = adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFlux")));

		assertTrue(returnType.parameterized());

		ParameterizedType parameterizedType = returnType.as(ParameterizedType.class);

		assertEquals(Collection.class, parameterizedType.getRawType());
		assertEquals(Object.class, parameterizedType.getActualTypeArguments()[0]);
	}

	interface SomeType {

		@OnCircuitBreaker
		Flux<String> fluxOnCircuitBreaker();

		@SuppressWarnings("rawtypes")
		Flux dumbFlux();
		
		Flux<String> fluxWithoutCircuitBreaker();

		@OnCircuitBreaker
		String onCircuitBreakerOtherType();
	}

	static class SomeFallback {
		
		Collection<String> fluxOnCircuitBreaker() {
			return Collections.singleton("fallback result");
		}
	}
}
