package com.github.ljtfreitas.restify.http.client.call.handler.rxjava2;

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
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2MaybeEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private RxJava2MaybeEndpointCallHandlerAdapter<String, String> adapter;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.single();

		adapter = new RxJava2MaybeEndpointCallHandlerAdapter<>(scheduler);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJava2Maybe() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("maybe"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJava2Maybe() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfRxJava2Maybe() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("maybe"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJava2MaybeIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbMaybe"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateHandlerFromEndpointMethodWithRxJava2MaybeReturnType() throws Exception {
		AsyncEndpointCallHandler<Maybe<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("maybe")), delegate);

		String result = "maybe result";

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(result));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Maybe<String> maybe = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(maybe);

		TestObserver<String> subscriber = maybe.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);
	}

	@Test
	public void shouldSubscribeErrorOnMaybeWhenCreatedHandlerWithRxJava2MaybeReturnTypeThrowException() throws Exception {
		EndpointCallHandler<Maybe<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("maybe")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(future);

		Maybe<String> maybe = handler.handle(asyncEndpointCall, null);

		assertNotNull(maybe);

		TestObserver<String> subscriber = maybe.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertError(exception);
	}

	interface SomeType {

		Maybe<String> maybe();

		@SuppressWarnings("rawtypes")
		Maybe dumbMaybe();

		String string();
	}
}
