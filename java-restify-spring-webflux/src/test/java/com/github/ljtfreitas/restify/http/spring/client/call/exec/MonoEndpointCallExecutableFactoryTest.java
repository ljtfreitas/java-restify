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

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class MonoEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private EndpointCall<String> endpointCallMock;

	private MonoEndpointCallExecutableFactory<String, String> factory;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.single();

		factory = new MonoEndpointCallExecutableFactory<>(scheduler);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsMono() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("mono"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotMono() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfMono() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("mono"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenMonoIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbMono"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithMonoReturnType() throws Exception {
		EndpointCallExecutable<Mono<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("mono")), delegate);

		String result = "mono result";

		when(delegate.execute(endpointCallMock, null))
			.thenReturn(result);

		Mono<String> mono = executable.execute(endpointCallMock, null);

		assertNotNull(mono);

		StepVerifier.create(mono)
			.expectNext(result)
			.expectComplete()
			.verify();

		verify(delegate).execute(endpointCallMock, null);
	}

	@Test
	public void shouldSubscribeErrorOnSingleWhenCreatedExecutableWithRxJava2SingleReturnTypeThrowException() throws Exception {
		EndpointCallExecutable<Mono<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("mono")), delegate);

		RuntimeException exception = new RuntimeException();

		when(delegate.execute(endpointCallMock, null))
			.thenThrow(exception);

		Mono<String> mono = executable.execute(endpointCallMock, null);

		assertNotNull(mono);

		StepVerifier.create(mono)
			.expectError()
			.verify();

		verify(delegate).execute(endpointCallMock, null);
	}

	interface SomeType {

		Mono<String> mono();

		@SuppressWarnings("rawtypes")
		Mono dumbMono();

		String string();
	}
}
