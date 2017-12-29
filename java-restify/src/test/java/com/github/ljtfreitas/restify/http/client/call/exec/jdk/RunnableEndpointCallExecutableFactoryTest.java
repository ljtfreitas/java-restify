package com.github.ljtfreitas.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.RunnableEndpointCallExecutableFactory;

@RunWith(MockitoJUnitRunner.class)
public class RunnableEndpointCallExecutableFactoryTest {

	private RunnableEndpointCallExecutableFactory factory;

	@Mock
	private EndpointCall<Void> call;

	@Before
	public void setup() {
		factory = new RunnableEndpointCallExecutableFactory();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRunnable() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("runnable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotRunnable() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRunnableReturnType() throws Exception {
		EndpointCallExecutable<Runnable, Void> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("runnable")));

		Runnable runnable = executable.execute(call, null);

		runnable.run();

		verify(call).execute();
	}

	interface SomeType {

		Runnable runnable();

		String string();
	}
}
