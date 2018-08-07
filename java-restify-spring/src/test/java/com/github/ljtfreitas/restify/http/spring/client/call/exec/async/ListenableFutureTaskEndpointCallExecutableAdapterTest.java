package com.github.ljtfreitas.restify.http.spring.client.call.exec.async;

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
import org.springframework.util.concurrent.ListenableFutureTask;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class ListenableFutureTaskEndpointCallExecutableAdapterTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private ListenableFutureTaskEndpointCallExecutableAdapter<String, String> adapter;

	@Before
	public void setup() {
		adapter = new ListenableFutureTaskEndpointCallExecutableAdapter<>();

		when(delegate.execute(any(), anyVararg()))
			.then((invocation) -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsListenableFutureTask() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("future"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsListenableFutureTask() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfListenableFutureTask() throws Exception {
		assertEquals(JavaType.of(String.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("future"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenListenableFutureTaskIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFuture"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithListenableFutureTaskReturnType() throws Exception {
		EndpointCallExecutable<ListenableFutureTask<String>, String> executable = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("future")), delegate);

		String result = "future result";

		ListenableFutureTask<String> future = executable.execute(() -> result, null);

		assertEquals(result, future.get());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		ListenableFutureTask<String> future();

		@SuppressWarnings("rawtypes")
		ListenableFutureTask dumbFuture();

		String string();
	}
}
