package com.github.ljtfreitas.restify.http.spring.client.call.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.reflection.JavaType;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class FluxEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private EndpointCall<String> endpointCallMock;

	private FluxEndpointCallExecutableFactory<String, String> factory;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.single();

		factory = new FluxEndpointCallExecutableFactory<>(scheduler);
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
		EndpointCallExecutable<Flux<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("flux")), delegate);

		String result = "flux result";

		when(delegate.execute(endpointCallMock, null))
			.thenReturn(result);

		Flux<String> flux = executable.execute(endpointCallMock, null);

		assertNotNull(flux);

		StepVerifier.create(flux)
			.expectNext(result)
			.expectComplete()
			.verify();

		verify(delegate).execute(endpointCallMock, null);
	}

	@Test
	public void shouldSubscribeErrorOnObservableWhenCreatedExecutableWithRxJava2ObservableReturnTypeThrowException() throws Exception {
		EndpointCallExecutable<Flux<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("flux")), delegate);

		RuntimeException exception = new RuntimeException();

		when(delegate.execute(endpointCallMock, null))
			.thenThrow(exception);

		Flux<String> flux = executable.execute(endpointCallMock, null);

		assertNotNull(flux);

		StepVerifier.create(flux)
			.expectError()
			.verify();

		verify(delegate).execute(endpointCallMock, null);
	}

	interface SomeType {

		Flux<String> flux();

		@SuppressWarnings("rawtypes")
		Flux dumbFlux();

		String string();
	}
}
