package com.github.ljtfreitas.restify.http.client.call.handler.rxjava2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
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

import io.reactivex.Completable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2CompletableEndpointCallHandlerFactoryTest {

	@Mock
	private AsyncEndpointCall<Void> asyncEndpointCallMock;

	@Mock
	private EndpointCall<Void> endpointCall;

	private RxJava2CompletableEndpointCallHandlerFactory adapter;

	@Before
	public void setup() {
		adapter = new RxJava2CompletableEndpointCallHandlerFactory(Schedulers.single());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJava2Completable() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("completable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJava2Completable() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithRxJava2CompletableReturnType() throws Exception {
		AsyncEndpointCallHandler<Completable, Void> handler = adapter
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		when(asyncEndpointCallMock.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(null));

		Completable completable = handler.handleAsync(asyncEndpointCallMock, null);

		assertNotNull(completable);

		TestObserver<Void> subscriber = completable.test();
		subscriber.await();

		subscriber.assertNoErrors();
		subscriber.assertNoValues();
		subscriber.assertComplete();

		verify(asyncEndpointCallMock).executeAsync();
	}

	@Test
	public void shouldSubscribeErrorOnCompletableWhenCreatedHandlerWithRxJava2CompletableReturnTypeThrowException() throws Exception {
		AsyncEndpointCallHandler<Completable, Void> handler = adapter
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		RuntimeException exception = new RuntimeException();

		CompletableFuture<Void> futureAsException = new CompletableFuture<>();
		futureAsException.completeExceptionally(exception);

		when(asyncEndpointCallMock.executeAsync())
			.thenReturn(futureAsException);

		Completable completable = handler.handleAsync(asyncEndpointCallMock, null);

		assertNotNull(completable);

		TestObserver<Void> subscriber = completable.test();

		subscriber.await()
			.assertError(exception);

		verify(asyncEndpointCallMock).executeAsync();
	}

	@Test
	public void shouldCreateSyncHandlerFromEndpointMethodWithRxJava2CompletableReturnType() throws Exception {
		EndpointCallHandler<Completable, Void> handler = adapter
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("completable")));

		Completable completable = handler.handle(endpointCall, null);

		assertNotNull(completable);

		TestObserver<Void> subscriber = completable.test();
		subscriber.await();

		subscriber.assertNoErrors();
		subscriber.assertNoValues();
		subscriber.assertComplete();

		verify(endpointCall).execute();
	}

	interface SomeType {

		Completable completable();

		String string();
	}
}
