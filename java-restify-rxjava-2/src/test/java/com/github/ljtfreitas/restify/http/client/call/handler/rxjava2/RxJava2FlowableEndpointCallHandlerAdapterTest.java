package com.github.ljtfreitas.restify.http.client.call.handler.rxjava2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
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

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2FlowableEndpointCallHandlerAdapterTest {

	@Mock
	private AsyncEndpointCallHandler<Collection<String>, Collection<String>> delegate;

	@Mock
	private AsyncEndpointCall<Collection<String>> endpointCallMock;

	private RxJava2FlowableEndpointCallHandlerAdapter<String, Collection<String>> adapter;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.single();

		adapter = new RxJava2FlowableEndpointCallHandlerAdapter<>(scheduler);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJava2Flowable() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("flowable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJava2Flowable() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfRxJava2Flowable() throws Exception {
		assertEquals(JavaType.of(JavaType.parameterizedType(Collection.class, String.class)), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("flowable"))));
	}

	@Test
	public void shouldReturnCollectionWithObjectTypeWhenRxJava2FlowableIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(JavaType.parameterizedType(Collection.class, Object.class)), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFlowable"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateHandlerFromEndpointMethodWithRxJava2FlowableReturnType() throws Exception {
		AsyncEndpointCallHandler<Flowable<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("flowable")), delegate);

		String result = "flowable result";

		when(endpointCallMock.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(Arrays.asList(result)));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());

		Flowable<String> flowable = handler.handleAsync(endpointCallMock, null);

		assertNotNull(flowable);

		TestSubscriber<String> subscriber = flowable.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);

		verify(endpointCallMock).executeAsync();
		verify(delegate).handle(notNull(EndpointCall.class), anyVararg());
	}

	@Test
	public void shouldSubscribeErrorOnFlowableWhenCreatedHandlerWithRxJava2FlowableReturnTypeThrowException() throws Exception {
		AsyncEndpointCallHandler<Flowable<String>, Collection<String>> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("flowable")), delegate);

		RuntimeException exception = new RuntimeException();

		CompletableFuture<Collection<String>> futureAsException = new CompletableFuture<>();
		futureAsException.completeExceptionally(exception);

		when(endpointCallMock.executeAsync())
			.thenReturn(futureAsException);

		Flowable<String> flowable = handler.handleAsync(endpointCallMock, null);

		assertNotNull(flowable);

		TestSubscriber<String> subscriber = flowable.subscribeOn(scheduler).test();

		subscriber.await()
			.assertError(exception);

		verify(endpointCallMock).executeAsync();
	}

	@Test
	public void shouldCreateSyncHandlerFromEndpointMethodWithRxJava2FlowableReturnType() throws Exception {
		EndpointCallHandler<Flowable<String>, Collection<String>> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("flowable")), delegate);

		String result = "flowable result";

		when(delegate.handle(endpointCallMock, null))
			.thenReturn(Arrays.asList(result));

		Flowable<String> flowable = handler.handle(endpointCallMock, null);

		assertNotNull(flowable);

		TestSubscriber<String> subscriber = flowable.subscribeOn(scheduler).test();

		subscriber.await()
			.assertNoErrors()
			.assertComplete()
			.assertResult(result);

		verify(delegate).handle(endpointCallMock, null);
	}

	interface SomeType {

		Flowable<String> flowable();

		@SuppressWarnings("rawtypes")
		Flowable dumbFlowable();

		String string();
	}
}
