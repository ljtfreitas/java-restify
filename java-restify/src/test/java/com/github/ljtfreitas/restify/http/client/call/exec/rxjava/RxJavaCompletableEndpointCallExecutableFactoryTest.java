package com.github.ljtfreitas.restify.http.client.call.exec.rxjava;

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
import com.github.ljtfreitas.restify.http.contract.metadata.SimpleEndpointMethod;

import rx.Completable;
import rx.Scheduler;
import rx.observers.AssertableSubscriber;
import rx.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJavaCompletableEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCall<Void> endpointCallMock;

	private RxJavaCompletableEndpointCallExecutableFactory factory;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.immediate();

		factory = new RxJavaCompletableEndpointCallExecutableFactory(scheduler);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJavaCompletable() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("completable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJavaCompletable() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRxJavaCompletableReturnType() throws Exception {
		EndpointCallExecutable<Completable, Void> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		Completable completable = executable.execute(endpointCallMock, null);

		assertNotNull(completable);

		AssertableSubscriber<Void> subscriber = completable.subscribeOn(scheduler).test();

		subscriber.assertCompleted()
			.assertNoErrors()
			.assertNoValues();

		verify(endpointCallMock).execute();
	}

	@Test
	public void shouldSubscribeErrorOnCompletableWhenCreatedExecutableWithRxJavaCompletableReturnTypeThrowException() throws Exception {
		EndpointCallExecutable<Completable, Void> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		RuntimeException exception = new RuntimeException();

		when(endpointCallMock.execute())
			.thenThrow(exception);

		Completable completable = executable.execute(endpointCallMock, null);

		assertNotNull(completable);

		AssertableSubscriber<Void> subscriber = completable.subscribeOn(scheduler).test();

		subscriber.assertError(exception);

		verify(endpointCallMock).execute();
	}

	interface SomeType {

		Completable completable();

		String string();
	}
}
