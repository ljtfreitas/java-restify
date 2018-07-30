package com.github.ljtfreitas.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class CompletionStageEndpointCallExecutableFactoryTest {

	@Mock
	private AsyncEndpointCallExecutable<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private CompletionStageEndpointCallExecutableFactory<String, String> factory;

	@Before
	public void setup() {
		factory = new CompletionStageEndpointCallExecutableFactory<>(r -> r.run());

		when(delegate.execute(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsCompletionStage() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("stage"))));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsCompletableFuture() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("future"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotAssignableFromCompletionStage() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfCompletableFuture() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("future"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenCompletableFutureIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFuture"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithCompletableFutureReturnType() throws Exception {
		AsyncEndpointCallExecutable<CompletionStage<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("future")), delegate);

		String result = "future result";

		when(asyncEndpointCall.executeAsync()).thenReturn(CompletableFuture.completedFuture(result));

		CompletionStage<String> future = executable.executeAsync(asyncEndpointCall, null);

		assertEquals(result, future.toCompletableFuture().get());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		CompletionStage<String> stage();

		CompletableFuture<String> future();

		@SuppressWarnings("rawtypes")
		CompletableFuture dumbFuture();

		String string();
	}
}
