package com.github.ljtfreitas.restify.http.client.call.handler.rxjava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
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

import rx.Observable;
import rx.observers.AssertableSubscriber;
import rx.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJavaObservableEndpointCallHandlerAdapterTest {

	@Mock
	private AsyncEndpointCallHandler<Collection<String>, Collection<String>> delegate;

	@Mock
	private AsyncEndpointCall<Collection<String>> asyncEndpointCallMock;

	@Mock
	private EndpointCall<Collection<String>> endpointCallMock;
	
	private RxJavaObservableEndpointCallHandlerAdapter<String, Collection<String>> adapter;

	@Before
	public void setup() {
		adapter = new RxJavaObservableEndpointCallHandlerAdapter<>(Schedulers.immediate());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJavaObservable() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("observable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJavaObservable() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnCollectionWithArgumentTypeOfRxJavObservable() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("observable"))));
	}

	@Test
	public void shouldReturnCollectionWithObjectTypeWhenRxJavaObservableIsNotParameterized() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbObservable"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateHandlerFromEndpointMethodWithRxJavaObservableReturnType() throws Exception {
		AsyncEndpointCallHandler<Observable<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		String result = "observable result";

		when(asyncEndpointCallMock.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(Arrays.asList(result)));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Observable<String> observable = handler.handleAsync(asyncEndpointCallMock, null);

		assertNotNull(observable);

		AssertableSubscriber<String> subscriber = observable.test();

		subscriber.assertValue(result)
			.assertCompleted()
			.assertNoErrors();

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
	}

	@Test
	public void shouldSubscribeErrorOnObservableWhenCreatedHandlerWithRxJavaObservableReturnTypeThrowException() throws Exception {
		AsyncEndpointCallHandler<Observable<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<Collection<String>> futureAsException = new CompletableFuture<>();
		futureAsException.completeExceptionally(exception);
		
		when(asyncEndpointCallMock.executeAsync())
			.thenReturn(futureAsException);

		Observable<String> observable = handler.handleAsync(asyncEndpointCallMock, null);

		assertNotNull(observable);

		AssertableSubscriber<String> subscriber = observable.test();

		subscriber.assertError(exception);

		verify(delegate, never()).handle(any(), anyVararg());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateSyncHandlerFromEndpointMethodWithRxJavaObservableReturnType() throws Exception {
		EndpointCallHandler<Observable<String>, Collection<String>> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		String result = "observable result";

		when(endpointCallMock.execute())
			.thenReturn(Arrays.asList(result));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Observable<String> observable = handler.handle(endpointCallMock, null);

		assertNotNull(observable);

		AssertableSubscriber<String> subscriber = observable.test();

		subscriber.assertValue(result)
			.assertCompleted()
			.assertNoErrors();

		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
	}

	
	interface SomeType {

		Observable<String> observable();

		@SuppressWarnings("rawtypes")
		Observable dumbObservable();

		String string();
	}
}
