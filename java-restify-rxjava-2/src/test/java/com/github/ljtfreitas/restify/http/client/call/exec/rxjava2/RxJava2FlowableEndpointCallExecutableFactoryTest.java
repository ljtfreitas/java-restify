package com.github.ljtfreitas.restify.http.client.call.exec.rxjava2;

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

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(MockitoJUnitRunner.class)
public class RxJava2FlowableEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private EndpointCall<String> endpointCallMock;

	private RxJava2FlowableEndpointCallExecutableFactory<String, String> factory;

	private Scheduler scheduler;

	@Before
	public void setup() {
		scheduler = Schedulers.single();

		factory = new RxJava2FlowableEndpointCallExecutableFactory<>(scheduler);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRxJava2Flowable() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("flowable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRxJava2Flowable() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfRxJava2Flowable() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("flowable"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenRxJava2FlowableIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFlowable"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRxJava2FlowableReturnType() throws Exception {
		EndpointCallExecutable<Flowable<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("flowable")), delegate);

		String result = "flowable result";

		when(delegate.execute(endpointCallMock, null))
			.thenReturn(result);

		Flowable<String> flowable = executable.execute(endpointCallMock, null);

		assertNotNull(flowable);

		TestSubscriber<String> subscriber = flowable.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertNoErrors()
			.assertComplete()
			.assertResult(result);

		verify(delegate).execute(endpointCallMock, null);
	}

	@Test
	public void shouldSubscribeErrorOnFlowableWhenCreatedExecutableWithRxJava2FlowableReturnTypeThrowException() throws Exception {
		EndpointCallExecutable<Flowable<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("flowable")), delegate);

		RuntimeException exception = new RuntimeException();

		when(delegate.execute(endpointCallMock, null))
			.thenThrow(exception);

		Flowable<String> flowable = executable.execute(endpointCallMock, null);

		assertNotNull(flowable);

		TestSubscriber<String> subscriber = flowable.subscribeOn(scheduler).test();
		subscriber.await();

		subscriber.assertError(exception);

		verify(delegate).execute(endpointCallMock, null);
	}

	interface SomeType {

		Flowable<String> flowable();

		@SuppressWarnings("rawtypes")
		Flowable dumbFlowable();

		String string();
	}
}
