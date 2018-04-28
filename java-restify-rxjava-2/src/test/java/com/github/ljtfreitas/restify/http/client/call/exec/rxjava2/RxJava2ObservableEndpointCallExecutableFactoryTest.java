package com.github.ljtfreitas.restify.http.client.call.exec.rxjava2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2ObservableEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private RxJava2ObservableEndpointCallExecutableFactory<String, String> factory;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.single();

		factory = new RxJava2ObservableEndpointCallExecutableFactory<>(scheduler);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJava2Observable() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("observable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJava2Observable() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfRxJava2Observable() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("observable"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJava2ObservableIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbObservable"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRxJava2ObservableReturnType() throws Exception {
		AsyncEndpointCallExecutable<Observable<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		String result = "observable result";

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(result));

		when(delegate.execute(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Observable<String> observable = executable.execute(asyncEndpointCall, null);

		assertNotNull(observable);

		TestObserver<String> subscriber = observable.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);
	}

	@Test
	public void shouldSubscribeErrorOnObservableWhenCreatedExecutableWithRxJava2ObservableReturnTypeThrowException() throws Exception {
		AsyncEndpointCallExecutable<Observable<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("observable")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(future);

		Observable<String> observable = executable.execute(asyncEndpointCall, null);

		assertNotNull(observable);

		TestObserver<String> subscriber = observable.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertError(ExecutionException.class);
	}

	interface SomeType {

		Observable<String> observable();

		@SuppressWarnings("rawtypes")
		Observable dumbObservable();

		String string();
	}
}
