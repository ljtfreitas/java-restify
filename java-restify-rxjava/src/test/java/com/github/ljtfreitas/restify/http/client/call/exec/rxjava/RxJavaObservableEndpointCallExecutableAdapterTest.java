package com.github.ljtfreitas.restify.http.client.call.exec.rxjava;

import static org.junit.Assert.assertEquals;
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
import com.github.ljtfreitas.restify.reflection.JavaType;

import rx.Observable;
import rx.Scheduler;
import rx.observers.AssertableSubscriber;
import rx.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJavaObservableEndpointCallExecutableAdapterTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private EndpointCall<String> endpointCallMock;

	private RxJavaObservableEndpointCallExecutableAdapter<String, String> adapter;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.immediate();

		adapter = new RxJavaObservableEndpointCallExecutableAdapter<>(scheduler);

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
	public void shouldReturnArgumentTypeOfRxJavObservable() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("observable"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJavaObservableIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbObservable"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRxJavaObservableReturnType() throws Exception {
		EndpointCallExecutable<Observable<String>, String> executable = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		String result = "observable result";

		when(delegate.execute(endpointCallMock, null))
			.thenReturn(result);

		Observable<String> observable = executable.execute(endpointCallMock, null);

		assertNotNull(observable);

		AssertableSubscriber<String> subscriber = observable.subscribeOn(scheduler).test();

		subscriber.assertCompleted()
			.assertNoErrors()
			.assertValue(result);

		verify(delegate).execute(endpointCallMock, null);
	}

	@Test
	public void shouldSubscribeErrorOnObservableWhenCreatedExecutableWithRxJavaObservableReturnTypeThrowException() throws Exception {
		EndpointCallExecutable<Observable<String>, String> executable = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		RuntimeException exception = new RuntimeException();

		when(delegate.execute(endpointCallMock, null))
			.thenThrow(exception);

		Observable<String> observable = executable.execute(endpointCallMock, null);

		assertNotNull(observable);

		AssertableSubscriber<String> subscriber = observable.subscribeOn(scheduler).test();

		subscriber.assertError(exception);

		verify(delegate).execute(endpointCallMock, null);
	}

	interface SomeType {

		Observable<String> observable();

		@SuppressWarnings("rawtypes")
		Observable dumbObservable();

		String string();
	}
}
