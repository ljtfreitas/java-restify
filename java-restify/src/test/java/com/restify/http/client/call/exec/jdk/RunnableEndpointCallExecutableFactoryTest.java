package com.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.SimpleEndpointMethod;

@RunWith(MockitoJUnitRunner.class)
public class RunnableEndpointCallExecutableFactoryTest {

	private RunnableEndpointCallExecutableFactory<String> factory;

	@Mock
	private EndpointCall<String> call;

	@Before
	public void setup() {
		factory = new RunnableEndpointCallExecutableFactory<>();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsRunnable() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("runnable"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsRunnable() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithRunnableReturnType() throws Exception {
		EndpointCallExecutable<Runnable, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("runnable")));

		Runnable runnable = executable.execute(call);

		runnable.run();

		verify(call).execute();
	}

	interface SomeType {

		Runnable runnable();

		String string();
	}
}
