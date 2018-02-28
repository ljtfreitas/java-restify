package com.github.ljtfreitas.restify.http.client.call.exec.async;

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
import com.github.ljtfreitas.restify.http.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class AsyncEndpointCallObjectExecutableFactoryTest {

	@Mock
	private AsyncEndpointCallExecutable<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private AsyncEndpointCallObjectExecutableFactory<String, String> factory;

	@Before
	public void setup() {
		factory = new AsyncEndpointCallObjectExecutableFactory<>(r -> r.run());

		when(delegate.execute(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsAsyncEndpointCall() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("call"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotAsyncEndpointCall() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfAsyncEndpointCall() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("call"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenAsyncEndpointCallIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbCall"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithAsyncEndpoinCallReturnType() throws Exception {
		AsyncEndpointCallExecutable<AsyncEndpointCall<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("call")), delegate);

		String result = "future result";

		when(asyncEndpointCall.executeAsync()).thenReturn(CompletableFuture.completedFuture(result));

		AsyncEndpointCall<String> output = executable.executeAsync(asyncEndpointCall, null);

		assertEquals(result, output.executeAsync().get());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}
	interface SomeType {

		AsyncEndpointCall<String> call();

		@SuppressWarnings("rawtypes")
		AsyncEndpointCall dumbCall();

		String string();
	}
}
