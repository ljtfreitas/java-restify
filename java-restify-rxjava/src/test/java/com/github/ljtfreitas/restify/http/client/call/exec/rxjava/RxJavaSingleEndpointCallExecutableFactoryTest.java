package com.github.ljtfreitas.restify.http.client.call.exec.rxjava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.reflection.JavaType;

import rx.Scheduler;
import rx.Single;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJavaSingleEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private EndpointCall<String> endpointCallMock;

	private RxJavaSingleEndpointCallExecutableFactory<String, String> factory;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.immediate();

		factory = new RxJavaSingleEndpointCallExecutableFactory<>(scheduler);

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJavaSingle() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("single"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJavaSingle() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}


	@Test
	public void shouldReturnArgumentTypeOfRxJavSingle() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("single"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJavaSingleIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbSingle"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRxJavaSingleReturnType() throws Exception {
		EndpointCallExecutable<Single<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		String result = "single result";

		when(delegate.execute(any(), anyVararg()))
			.thenReturn(result);

		Single<String> single = executable.execute(endpointCallMock, null);

		assertNotNull(single);

		TestSubscriber<String> subscriber = TestSubscriber.create();

		single.subscribeOn(scheduler).subscribe(subscriber);

		subscriber.assertCompleted();
		subscriber.assertNoErrors();
		subscriber.assertValue(result);
	}

	@Test
	public void shouldSubscribeErrorOnSingleWhenCreatedExecutableWithRxJavaSingleReturnTypeThrowException() throws Exception {
		EndpointCallExecutable<Single<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		RuntimeException exception = new RuntimeException();

		when(delegate.execute(any(), anyVararg()))
			.thenThrow(exception);

		Single<String> single = executable.execute(endpointCallMock, null);

		assertNotNull(single);

		TestSubscriber<String> subscriber = TestSubscriber.create();

		single.subscribeOn(scheduler).subscribe(subscriber);

		subscriber.assertError(exception);
	}

	interface SomeType {

		Single<String> single();

		@SuppressWarnings("rawtypes")
		Single dumbSingle();

		String string();
	}
}
