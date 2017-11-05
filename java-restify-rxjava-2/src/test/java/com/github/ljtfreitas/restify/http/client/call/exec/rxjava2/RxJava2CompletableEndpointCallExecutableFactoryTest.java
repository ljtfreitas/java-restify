package com.github.ljtfreitas.restify.http.client.call.exec.rxjava2;

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

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2CompletableEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCall<Void> endpointCallMock;

	private RxJava2CompletableEndpointCallExecutableFactory factory;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.single();

		factory = new RxJava2CompletableEndpointCallExecutableFactory(scheduler);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJava2Completable() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("completable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJava2Completable() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRxJava2CompletableReturnType() throws Exception {
		EndpointCallExecutable<Completable, Void> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		Completable completable = executable.execute(endpointCallMock, null);

		assertNotNull(completable);

		TestObserver<Void> subscriber = completable.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertNoErrors();
		subscriber.assertNoValues();
		subscriber.assertComplete();

		verify(endpointCallMock).execute();
	}

	@Test
	public void shouldSubscribeErrorOnCompletableWhenCreatedExecutableWithRxJava2CompletableReturnTypeThrowException() throws Exception {
		EndpointCallExecutable<Completable, Void> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		RuntimeException exception = new RuntimeException();

		when(endpointCallMock.execute())
			.thenThrow(exception);

		Completable completable = executable.execute(endpointCallMock, null);

		assertNotNull(completable);

		TestObserver<Void> subscriber = completable.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertError(exception);

		verify(endpointCallMock).execute();
	}

	interface SomeType {

		Completable completable();

		String string();
	}
}
