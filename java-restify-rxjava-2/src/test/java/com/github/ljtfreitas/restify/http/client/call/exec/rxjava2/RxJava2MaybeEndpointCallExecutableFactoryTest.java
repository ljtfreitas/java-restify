package com.github.ljtfreitas.restify.http.client.call.exec.rxjava2;

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
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2MaybeEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private RxJava2MaybeEndpointCallExecutableFactory<String, String> factory;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.single();

		factory = new RxJava2MaybeEndpointCallExecutableFactory<>(scheduler);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJava2Maybe() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("maybe"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJava2Maybe() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfRxJava2Maybe() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("maybe"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJava2MaybeIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbMaybe"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRxJava2MaybeReturnType() throws Exception {
		AsyncEndpointCallExecutable<Maybe<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("maybe")), delegate);

		String result = "maybe result";

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(result));

		when(delegate.execute(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Maybe<String> maybe = executable.executeAsync(asyncEndpointCall, null);

		assertNotNull(maybe);

		TestObserver<String> subscriber = maybe.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);
	}

	@Test
	public void shouldSubscribeErrorOnMaybeWhenCreatedExecutableWithRxJava2MaybeReturnTypeThrowException() throws Exception {
		EndpointCallExecutable<Maybe<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("maybe")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(future);

		Maybe<String> maybe = executable.execute(asyncEndpointCall, null);

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
