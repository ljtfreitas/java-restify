package com.github.ljtfreitas.restify.http.client.call.handler.guava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.MoreExecutors;

@RunWith(MockitoJUnitRunner.class)
public class ListenableFutureTaskEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	private ListenableFutureTaskEndpointCallHandlerAdapter<String, String> adapter;

	@Before
	public void setup() {
		adapter = new ListenableFutureTaskEndpointCallHandlerAdapter<>(MoreExecutors.newDirectExecutorService());

		when(delegate.handle(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsListenableFutureTask() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotListenableFutureTask() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfListenableFutureTask() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenListenableFutureTaskIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFutureTask"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithListenableFutureTaskReturnType() throws Exception {
		EndpointCallHandler<ListenableFutureTask<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask")), delegate);

		String result = "future result";

		ListenableFuture<String> future = handler.handle(() -> result, null);

		assertEquals(result, future.get());
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(any(), anyVararg());
	}

	interface SomeType {

		ListenableFutureTask<String> futureTask();

		@SuppressWarnings("rawtypes")
		ListenableFutureTask dumbFutureTask();

		String string();
	}
}
