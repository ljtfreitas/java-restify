package com.github.ljtfreitas.restify.http.client.call.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallObjectHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class EndpointCallObjectHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	private EndpointCallObjectHandlerAdapter<String, String> adapter;

	@Before
	public void setup() {
		adapter = new EndpointCallObjectHandlerAdapter<>();

		when(delegate.handle(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsEndpointCall() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("call"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotEndpointCall() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfEndpointCall() throws Exception {
		assertEquals(JavaType.of(String.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("call"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenEndpointCallIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbCall"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithEndpointCallReturnType() throws Exception {
		EndpointCallHandler<EndpointCall<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("call")), delegate);

		String result = "call";

		EndpointCall<String> call = handler.handle(() -> result, null);

		assertEquals(result, call.execute());
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(any(), anyVararg());
	}

	interface SomeType {

		EndpointCall<String> call();

		@SuppressWarnings("rawtypes")
		EndpointCall dumbCall();

		String string();
	}

	private class SimpleEndpointMethod extends EndpointMethod {

		public SimpleEndpointMethod(Method javaMethod) {
			super(javaMethod, "/", "GET");
		}

		public SimpleEndpointMethod(Method javaMethod, EndpointMethodParameters parameters) {
			super(javaMethod, "/", "GET", parameters);
		}
	}

}
