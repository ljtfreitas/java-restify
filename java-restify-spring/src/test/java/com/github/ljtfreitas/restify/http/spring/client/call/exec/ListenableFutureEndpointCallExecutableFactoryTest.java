package com.github.ljtfreitas.restify.http.spring.client.call.exec;

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
import org.springframework.util.concurrent.ListenableFuture;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.ListenableFutureEndpointCallExecutableFactory;

@RunWith(MockitoJUnitRunner.class)
public class ListenableFutureEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private ListenableFutureEndpointCallExecutableFactory<String, String> factory;

	@Before
	public void setup() {
		factory = new ListenableFutureEndpointCallExecutableFactory<>();

		when(delegate.execute(any(), anyVararg()))
			.then((invocation) -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsListenableFuture() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("future"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsListenableFuture() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfListenableFuture() throws Exception {
		assertEquals(JavaType.of(String.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("future"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenListenableFutureIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbFuture"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithListenableFutureReturnType() throws Exception {
		EndpointCallExecutable<ListenableFuture<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("future")), delegate);

		String result = "future result";

		ListenableFuture<String> future = executable.execute(() -> result, null);

		assertEquals(result, future.get());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		ListenableFuture<String> future();

		@SuppressWarnings("rawtypes")
		ListenableFuture dumbFuture();

		String string();
	}
}
