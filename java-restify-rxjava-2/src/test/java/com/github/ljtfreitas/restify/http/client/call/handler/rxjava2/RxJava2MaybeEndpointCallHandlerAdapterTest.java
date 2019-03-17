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
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2MaybeEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	@Mock
	private EndpointCall<String> endpointCall;

	private RxJava2MaybeEndpointCallHandlerAdapter<String, String> adapter;

	@Before
	public void setup() {
		adapter = new RxJava2MaybeEndpointCallHandlerAdapter<>(Schedulers.single());
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

		TestObserver<String> subscriber = maybe.test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateMaybeOnHandlerWhenAsyncEndpointCallResultIsNull() throws Exception {
		AsyncEndpointCallHandler<Maybe<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("maybe")), delegate);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(null));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Maybe<String> maybe = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(maybe);

		TestObserver<String> subscriber = maybe.test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete();
	}

	@Test
	public void shouldSubscribeErrorOnMaybeWhenCreatedHandlerWithRxJava2MaybeReturnTypeThrowException() throws Exception {
		AsyncEndpointCallHandler<Maybe<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("maybe")), delegate);

		IllegalArgumentException exception = new IllegalArgumentException("ooops");

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(future);

		Maybe<String> maybe = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(maybe);

		TestObserver<String> subscriber = maybe.test();

		subscriber.await()
				  .assertError(exception);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateSyncHandlerFromEndpointMethodWithRxJava2MaybeReturnType() throws Exception {
		EndpointCallHandler<Maybe<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("maybe")), delegate);

		String result = "maybe result";

		when(endpointCall.execute())
			.thenReturn(result);

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Maybe<String> maybe = handler.handle(endpointCall, null);

		assertNotNull(maybe);

		TestObserver<String> subscriber = maybe.test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);
	}

	interface SomeType {

		Maybe<String> maybe();

		@SuppressWarnings("rawtypes")
		Maybe dumbMaybe();

		String string();
	}
}
