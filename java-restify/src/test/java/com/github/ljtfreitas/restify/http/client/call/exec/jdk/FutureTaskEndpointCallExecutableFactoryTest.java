package com.github.ljtfreitas.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class FutureTaskEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private FutureTaskEndpointCallExecutableFactory<String, String> factory;

	@Before
	public void setup() {
		ExecutorService executor = Executors.newSingleThreadExecutor();

		factory = new FutureTaskEndpointCallExecutableFactory<>(executor);

		when(delegate.execute(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsFutureTask() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotFutureTask() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfFutureTask() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenFutureTaskIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFutureTask"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithFutureTaskReturnType() throws Exception {
		EndpointCallExecutable<FutureTask<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("futureTask")), delegate);

		String result = "future task result";

		FutureTask<String> future = executable.execute(() -> result, null);

		assertEquals(result, future.get());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		FutureTask<String> futureTask();

		@SuppressWarnings("rawtypes")
		FutureTask dumbFutureTask();

		String string();
	}
}
