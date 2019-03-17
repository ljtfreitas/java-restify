package com.github.ljtfreitas.restify.http.client.call.handler.rxjava;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

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

import rx.Completable;
import rx.observers.AssertableSubscriber;
import rx.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJavaCompletableEndpointCallHandlerFactoryTest {

	@Mock
	private AsyncEndpointCall<Void> asyncEndpointCall;

	@Mock
	private EndpointCall<Void> endpointCall;

	private RxJavaCompletableEndpointCallHandlerFactory adapter;

	@Before
	public void setup() {
		adapter = new RxJavaCompletableEndpointCallHandlerFactory(Schedulers.immediate());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJavaCompletable() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("completable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJavaCompletable() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithRxJavaCompletableReturnType() throws Exception {
		AsyncEndpointCallHandler<Completable, Void> handler = adapter
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(null));

		Completable completable = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(completable);

		AssertableSubscriber<Void> subscriber = completable.test();

		subscriber.assertCompleted()
			.assertNoErrors()
			.assertNoValues();
	}

	@Test
	public void shouldSubscribeErrorOnCompletableWhenCreatedHandlerWithRxJavaCompletableReturnTypeThrowException() throws Exception {
		AsyncEndpointCallHandler<Completable, Void> handler = adapter
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		RuntimeException exception = new RuntimeException();

		CompletableFuture<Void> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(future);

		Completable completable = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(completable);

		AssertableSubscriber<Void> subscriber = completable.test();

		subscriber.assertError(exception);
	}

	@Test
	public void shouldCreateSyncHandlerFromEndpointMethodWithRxJavaCompletableReturnType() throws Exception {
		EndpointCallHandler<Completable, Void> handler = adapter
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		when(endpointCall.execute())
			.thenReturn(null);

		Completable completable = handler.handle(endpointCall, null);

		assertNotNull(completable);

		AssertableSubscriber<Void> subscriber = completable.test();

		subscriber.assertCompleted()
			.assertNoErrors()
			.assertNoValues();
	}

	interface SomeType {

		Completable completable();

		String string();
	}
}
