package com.github.ljtfreitas.restify.http.client.call.handler.rxjava2;

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
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2FlowableEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	@Mock
	private EndpointCall<String> endpointCallMock;

	private RxJava2FlowableEndpointCallHandlerAdapter<String, String> adapter;

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
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("flowable"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJava2FlowableIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFlowable"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRxJava2FlowableReturnType() throws Exception {
		EndpointCallHandler<Flowable<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("flowable")), delegate);

		String result = "flowable result";

		when(delegate.handle(endpointCallMock, null))
			.thenReturn(result);

		Flowable<String> flowable = handler.handle(endpointCallMock, null);

		assertNotNull(flowable);

		TestSubscriber<String> subscriber = flowable.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);

		verify(delegate).handle(endpointCallMock, null);
	}

	@Test
	public void shouldSubscribeErrorOnFlowableWhenCreatedExecutableWithRxJava2FlowableReturnTypeThrowException() throws Exception {
		EndpointCallHandler<Flowable<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("flowable")), delegate);

		RuntimeException exception = new RuntimeException();

		when(delegate.handle(endpointCallMock, null))
			.thenThrow(exception);

		Flowable<String> flowable = handler.handle(endpointCallMock, null);

		assertNotNull(flowable);

		TestSubscriber<String> subscriber = flowable.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertError(exception);

		verify(delegate).handle(endpointCallMock, null);
	}

	interface SomeType {

		Flowable<String> flowable();

		@SuppressWarnings("rawtypes")
		Flowable dumbFlowable();

		String string();
	}
}
