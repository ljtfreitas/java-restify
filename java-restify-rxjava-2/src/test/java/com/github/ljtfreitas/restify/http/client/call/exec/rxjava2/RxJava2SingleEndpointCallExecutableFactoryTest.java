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

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2SingleEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private RxJava2SingleEndpointCallExecutableFactory<String, String> factory;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.single();

		factory = new RxJava2SingleEndpointCallExecutableFactory<>(scheduler);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJava2Single() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("single"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJava2Single() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfRxJava2Single() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("single"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJava2SingleIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbSingle"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRxJava2SingleReturnType() throws Exception {
		AsyncEndpointCallExecutable<Single<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		String result = "single result";

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(result));

		when(delegate.execute(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Single<String> single = executable.execute(asyncEndpointCall, null);

		assertNotNull(single);

		TestObserver<String> subscriber = single.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);
	}

	@Test
	public void shouldSubscribeErrorOnSingleWhenCreatedExecutableWithRxJava2SingleReturnTypeThrowException() throws Exception {
		EndpointCallExecutable<Single<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("single")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		when(asyncEndpointCall.executeAsync())
			.thenReturn(future);

		Single<String> single = executable.execute(asyncEndpointCall, null);

		assertNotNull(single);

		TestObserver<String> subscriber = single.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertError(t -> t.getCause().equals(exception));
	}

	interface SomeType {

		Single<String> single();

		@SuppressWarnings("rawtypes")
		Single dumbSingle();

		String string();
	}
}
