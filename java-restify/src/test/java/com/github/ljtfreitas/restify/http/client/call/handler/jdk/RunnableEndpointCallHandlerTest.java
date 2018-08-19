package com.github.ljtfreitas.restify.http.client.call.handler.jdk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.SimpleEndpointMethod;

@RunWith(MockitoJUnitRunner.class)
public class RunnableEndpointCallHandlerTest {

	private RunnableEndpointCallHandlerFactory adapter;

	@Mock
	private EndpointCall<Void> call;

	@Before
	public void setup() {
		adapter = new RunnableEndpointCallHandlerFactory();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRunnable() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("runnable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRunnable() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithRunnableReturnType() throws Exception {
		EndpointCallHandler<Runnable, Void> handler = adapter.create(new SimpleEndpointMethod(SomeType.class.getMethod("runnable")));

		Runnable runnable = handler.handle(call, null);

		runnable.run();

		verify(call).execute();
	}

	interface SomeType {

		Runnable runnable();

		String string();
	}
}
