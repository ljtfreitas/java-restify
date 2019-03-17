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

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2SingleEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	@Mock
	private EndpointCall<String> endpointCall;

	private RxJava2SingleEndpointCallHandlerAdapter<String, String> adapter;

	@Before
	public void setup() {
		adapter = new RxJava2SingleEndpointCallHandlerAdapter<>(Schedulers.single());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJava2Single() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("single"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJava2Single() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfRxJava2Single() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("single"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJava2SingleIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbSingle"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateHandlerFromEndpointMethodWithRxJava2SingleReturnType() throws Exception {
		AsyncEndpointCallHandler<Single<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		String result = "single result";

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(result));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Single<String> single = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(single);

		TestObserver<String> subscriber = single.test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);
	}

	@Test
	public void shouldSubscribeErrorOnSingleWhenCreatedHandlerWithRxJava2SingleReturnTypeThrowException() throws Exception {
		AsyncEndpointCallHandler<Single<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(future);

		Single<String> single = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(single);

		TestObserver<String> subscriber = single.test();

		subscriber.await()
			.assertError(exception);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateSyncHandlerFromEndpointMethodWithRxJava2SingleReturnType() throws Exception {
		EndpointCallHandler<Single<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		String result = "single result";

		when(endpointCall.execute())
			.thenReturn(result);
		
		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Single<String> single = handler.handle(endpointCall, null);

		assertNotNull(single);

		TestObserver<String> subscriber = single.test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);
	}

	interface SomeType {

		Single<String> single();

		@SuppressWarnings("rawtypes")
		Single dumbSingle();

		String string();
	}
}
