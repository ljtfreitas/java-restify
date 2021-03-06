package com.github.ljtfreitas.restify.http.client.call.handler.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.http.client.call.handler.async.AsyncEndpointCallObjectHandlerAdapter;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class AsyncEndpointCallObjectHandlerAdapterTest {

	@Mock
	private AsyncEndpointCallHandler<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private AsyncEndpointCallObjectHandlerAdapter<String, String> adapter;

	@Before
	public void setup() {
		adapter = new AsyncEndpointCallObjectHandlerAdapter<>(r -> r.run());

		when(delegate.handle(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsAsyncEndpointCall() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("call"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotAsyncEndpointCall() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfAsyncEndpointCall() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("call"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenAsyncEndpointCallIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbCall"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithAsyncEndpoinCallReturnType() throws Exception {
		AsyncEndpointCallHandler<AsyncEndpointCall<String>, String> handler = adapter
				.adaptAsync(new SimpleEndpointMethod(SomeType.class.getMethod("call")), delegate);

		String result = "future result";

		when(asyncEndpointCall.executeAsync()).thenReturn(CompletableFuture.completedFuture(result));

		AsyncEndpointCall<String> output = handler.handleAsync(asyncEndpointCall, null);

		assertEquals(result, output.executeAsync().toCompletableFuture().join());
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(any(), anyVararg());
	}

	interface SomeType {

		AsyncEndpointCall<String> call();

		@SuppressWarnings("rawtypes")
		AsyncEndpointCall dumbCall();

		String string();
	}
}
