package com.github.ljtfreitas.restify.http.spring.client.call.handler.async;

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
import org.springframework.scheduling.annotation.AsyncResult;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class AsyncResultEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	private AsyncResultEndpointCallHandlerAdapter<String, String> adapter;

	@Before
	public void setup() {
		adapter = new AsyncResultEndpointCallHandlerAdapter<>(r -> r.run());

		when(delegate.handle(any(), anyVararg()))
			.then((invocation) -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsAsyncResult() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("asyncResult"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsAsyncResult() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfAsyncResult() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("asyncResult"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenAsyncResultIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbAsyncResult"))));
	}

	@Test
	public void shouldCreateDecoratedHandlerFromEndpointMethod() throws Exception {
		EndpointCallHandler<AsyncResult<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("asyncResult")), delegate);

		String result = "async result";

		AsyncResult<String> asyncResult = handler.handle(() -> result, null);

		assertEquals(result, asyncResult.get());
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(any(), anyVararg());
	}

	interface SomeType {

		AsyncResult<String> asyncResult();

		@SuppressWarnings("rawtypes")
		AsyncResult dumbAsyncResult();

		String string();
	}

}
