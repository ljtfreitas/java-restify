package com.github.ljtfreitas.restify.http.client.call.exec.rxjava2;

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

import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2MaybeEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private EndpointCall<String> endpointCallMock;

	private RxJava2MaybeEndpointCallExecutableFactory<String, String> factory;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.single();

		factory = new RxJava2MaybeEndpointCallExecutableFactory<>(scheduler);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJava2Maybe() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("maybe"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJava2Maybe() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfRxJava2Maybe() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("maybe"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJava2MaybeIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbMaybe"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRxJava2MaybeReturnType() throws Exception {
		EndpointCallExecutable<Maybe<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("maybe")), delegate);

		String result = "maybe result";

		when(delegate.execute(endpointCallMock, null))
			.thenReturn(result);

		Maybe<String> maybe = executable.execute(endpointCallMock, null);

		assertNotNull(maybe);

		TestObserver<String> subscriber = maybe.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);

		verify(delegate).execute(endpointCallMock, null);
	}

	@Test
	public void shouldSubscribeErrorOnMaybeWhenCreatedExecutableWithRxJava2MaybeReturnTypeThrowException() throws Exception {
		EndpointCallExecutable<Maybe<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("maybe")), delegate);

		RuntimeException exception = new RuntimeException();

		when(delegate.execute(endpointCallMock, null))
			.thenThrow(exception);

		Maybe<String> maybe = executable.execute(endpointCallMock, null);

		assertNotNull(maybe);

		TestObserver<String> subscriber = maybe.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertError(exception);

		verify(delegate).execute(endpointCallMock, null);
	}

	interface SomeType {

		Maybe<String> maybe();

		@SuppressWarnings("rawtypes")
		Maybe dumbMaybe();

		String string();
	}
}
