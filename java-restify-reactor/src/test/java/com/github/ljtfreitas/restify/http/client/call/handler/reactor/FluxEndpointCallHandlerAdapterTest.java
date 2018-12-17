package com.github.ljtfreitas.restify.http.client.call.handler.reactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
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
import com.github.ljtfreitas.restify.reflection.JavaType;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class FluxEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<Collection<String>, Collection<String>> delegate;

	@Mock
	private AsyncEndpointCall<Collection<String>> asyncEndpointCall;

	private FluxEndpointCallHandlerAdapter<String, Collection<String>> adapter;

	private Scheduler scheduler;

	private String expectedAsyncResult;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		scheduler = Schedulers.single();

		adapter = new FluxEndpointCallHandlerAdapter<>(scheduler);

		expectedAsyncResult = "flux result";

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(Arrays.asList(expectedAsyncResult)));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsFlux() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("flux"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotFlux() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnCollectionWithArgumentTypeOfFlux() throws Exception {
		assertEquals(JavaType.of(JavaType.parameterizedType(Collection.class, String.class)), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("flux"))));
	}

	@Test
	public void shouldReturnCollectionWithObjectTypeWhenFluxIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(JavaType.parameterizedType(Collection.class, Object.class)), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFlux"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithFluxReturnType() throws Exception {
		AsyncEndpointCallHandler<Flux<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("flux")), delegate);

		Flux<String> flux = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(flux);

		StepVerifier.create(flux)
			.expectNext(expectedAsyncResult)
			.expectComplete()
			.verify();
	}

	@Test
	public void shouldSubscribeErrorOnObservableWhenCreatedHandlerWithRxJava2ObservableReturnTypeThrowException() throws Exception {
		AsyncEndpointCallHandler<Flux<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("flux")), delegate);

		IllegalArgumentException exception = new IllegalArgumentException("ooooops...");

		CompletableFuture<Collection<String>> completableFuture = new CompletableFuture<>();
		completableFuture.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(completableFuture);

		Flux<String> flux = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(flux);

		StepVerifier.create(flux)
			.expectError(exception.getClass())
			.verify();
	}

	@Test
	public void shouldCreateSyncHandlerFromEndpointMethodWithFluxReturnType() throws Exception {
		EndpointCallHandler<Flux<String>, Collection<String>> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("flux")), delegate);

		when(asyncEndpointCall.execute())
			.thenReturn(Arrays.asList(expectedAsyncResult));

		Flux<String> flux = handler.handle(asyncEndpointCall, null);

		assertNotNull(flux);

		StepVerifier.create(flux)
			.expectNext(expectedAsyncResult)
			.expectComplete()
			.verify();
	}

	interface SomeType {

		Flux<String> flux();

		@SuppressWarnings("rawtypes")
		Flux dumbFlux();

		String string();
	}
}
