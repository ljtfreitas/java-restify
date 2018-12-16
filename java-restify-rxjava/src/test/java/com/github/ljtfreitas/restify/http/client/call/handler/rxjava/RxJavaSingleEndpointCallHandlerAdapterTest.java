package com.github.ljtfreitas.restify.http.client.call.handler.rxjava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

import rx.Scheduler;
import rx.Single;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJavaSingleEndpointCallHandlerAdapterTest {

	@Mock
	private AsyncEndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private RxJavaSingleEndpointCallHandlerAdapter<String, String> adapter;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.immediate();

		adapter = new RxJavaSingleEndpointCallHandlerAdapter<>(scheduler);

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJavaSingle() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("single"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJavaSingle() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}


	@Test
	public void shouldReturnArgumentTypeOfRxJavSingle() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("single"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJavaSingleIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbSingle"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithRxJavaSingleReturnType() throws Exception {
		AsyncEndpointCallHandler<Single<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		String result = "single result";

		CompletableFuture<String> resultAsFuture = CompletableFuture.completedFuture(result);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(resultAsFuture);
		
		when(delegate.handle(any(), anyVararg()))
			.thenReturn(resultAsFuture.join());

		Single<String> single = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(single);

		TestSubscriber<String> subscriber = TestSubscriber.create();

		single.subscribeOn(scheduler).subscribe(subscriber);

		subscriber.assertCompleted();
		subscriber.assertNoErrors();
		subscriber.assertValue(result);
	}

	@Test
	public void shouldSubscribeErrorOnSingleWhenCreatedHandlerWithRxJavaSingleReturnTypeThrowException() throws Exception {
		AsyncEndpointCallHandler<Single<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<String> resultAsFuture = new CompletableFuture<>();
		resultAsFuture.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(resultAsFuture);

		Single<String> single = handler.handleAsync(asyncEndpointCall, null);

		assertNotNull(single);

		TestSubscriber<String> subscriber = TestSubscriber.create();

		single.subscribeOn(scheduler).subscribe(subscriber);

		subscriber.assertError(ExecutionException.class);
	}

	interface SomeType {

		Single<String> single();

		@SuppressWarnings("rawtypes")
		Single dumbSingle();

		String string();
	}
}
