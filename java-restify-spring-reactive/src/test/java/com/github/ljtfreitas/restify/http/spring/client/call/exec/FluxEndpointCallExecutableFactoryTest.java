package com.github.ljtfreitas.restify.http.spring.client.call.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;
import com.github.ljtfreitas.restify.reflection.JavaType;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class FluxEndpointCallExecutableFactoryTest {

	@Mock
	private AsyncEndpointCallExecutable<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private FluxEndpointCallExecutableFactory<String, String> factory;

	private Scheduler scheduler;

	private String expectedAsyncResult;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		scheduler = Schedulers.single();

		factory = new FluxEndpointCallExecutableFactory<>(scheduler);

		expectedAsyncResult = "flux result";

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(expectedAsyncResult));

		when(delegate.execute(notNull(EndpointCall.class), anyVararg()))
			.then(o -> o.getArgumentAt(0, EndpointCall.class).execute());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsFlux() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("flux"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotFlux() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfFlux() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("flux"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenFluxIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFlux"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithFluxReturnType() throws Exception {
		AsyncEndpointCallExecutable<Flux<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("flux")), delegate);

		Flux<String> flux = executable.executeAsync(asyncEndpointCall, null);

		assertNotNull(flux);

		StepVerifier.create(flux)
			.expectNext(expectedAsyncResult)
			.expectComplete()
			.verify();
	}

	@Test
	public void shouldSubscribeErrorOnObservableWhenCreatedExecutableWithRxJava2ObservableReturnTypeThrowException() throws Exception {
		AsyncEndpointCallExecutable<Flux<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("flux")), delegate);

		RuntimeException exception = new RuntimeException("ooooops...");

		CompletableFuture<String> completableFuture = new CompletableFuture<>();
		completableFuture.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(completableFuture);

		Flux<String> flux = executable.executeAsync(asyncEndpointCall, null);

		assertNotNull(flux);

		StepVerifier.create(flux)
			.expectError()
			.verify();
	}

	interface SomeType {

		Flux<String> flux();

		@SuppressWarnings("rawtypes")
		Flux dumbFlux();

		String string();
	}
}
