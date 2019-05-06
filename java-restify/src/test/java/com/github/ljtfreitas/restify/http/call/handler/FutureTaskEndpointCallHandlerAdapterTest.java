package com.github.ljtfreitas.restify.http.call.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.util.async.DisposableExecutors;

@RunWith(MockitoJUnitRunner.class)
public class FutureTaskEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	private FutureTaskEndpointCallHandlerAdapter<String, String> adapter;

	@Before
	public void setup() {
		ExecutorService executor = DisposableExecutors.newSingleThreadExecutor();

		adapter = new FutureTaskEndpointCallHandlerAdapter<>(executor);

		when(delegate.handle(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsFutureTask() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotFutureTask() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfFutureTask() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenFutureTaskIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFutureTask"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithFutureTaskReturnType() throws Exception {
		EndpointCallHandler<FutureTask<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask")), delegate);

		String result = "future task result";

		FutureTask<String> future = handler.handle(() -> result, null);

		assertEquals(result, future.get());
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(any(), anyVararg());
	}

	interface SomeType {

		FutureTask<String> futureTask();

		@SuppressWarnings("rawtypes")
		FutureTask dumbFutureTask();

		String string();
	}
}
