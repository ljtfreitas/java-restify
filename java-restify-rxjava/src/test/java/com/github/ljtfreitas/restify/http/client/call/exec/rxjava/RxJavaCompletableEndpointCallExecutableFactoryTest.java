package com.github.ljtfreitas.restify.http.client.call.exec.rxjava;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;

import rx.Completable;
import rx.Scheduler;
import rx.observers.AssertableSubscriber;
import rx.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJavaCompletableEndpointCallExecutableFactoryTest {

	@Mock
	private AsyncEndpointCall<Void> endpointCall;

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
		AsyncEndpointCallExecutable<Completable, Void> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		when(endpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(null));

		Completable completable = executable.executeAsync(endpointCall, null);

		assertNotNull(completable);

		AssertableSubscriber<Void> subscriber = completable.subscribeOn(scheduler).test();

		subscriber.assertCompleted()
			.assertNoErrors()
			.assertNoValues();
	}

	@Test
	public void shouldSubscribeErrorOnCompletableWhenCreatedExecutableWithRxJavaCompletableReturnTypeThrowException() throws Exception {
		AsyncEndpointCallExecutable<Completable, Void> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		RuntimeException exception = new RuntimeException();

		CompletableFuture<Void> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		when(endpointCall.executeAsync())
			.thenReturn(future);

		Completable completable = executable.execute(endpointCall, null);

		assertNotNull(completable);

		AssertableSubscriber<Void> subscriber = completable.subscribeOn(scheduler).test();

		subscriber.assertError(ExecutionException.class);
	}

	interface SomeType {

		Completable completable();

		String string();
	}
}
