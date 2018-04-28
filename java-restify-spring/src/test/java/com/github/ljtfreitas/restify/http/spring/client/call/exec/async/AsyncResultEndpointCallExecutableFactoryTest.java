package com.github.ljtfreitas.restify.http.spring.client.call.exec.async;

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
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class AsyncResultEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private AsyncResultEndpointCallExecutableFactory<String, String> factory;

	@Before
	public void setup() {
		factory = new AsyncResultEndpointCallExecutableFactory<>(r -> r.run());

		when(delegate.execute(any(), anyVararg()))
			.then((invocation) -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsAsyncResult() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("asyncResult"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsAsyncResult() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfAsyncResult() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("asyncResult"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenAsyncResultIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbAsyncResult"))));
	}

	@Test
	public void shouldCreateDecoratedExecutableFromEndpointMethod() throws Exception {
		EndpointCallExecutable<AsyncResult<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("asyncResult")), delegate);

		String result = "async result";

		AsyncResult<String> asyncResult = executable.execute(() -> result, null);

		assertEquals(result, asyncResult.get());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		AsyncResult<String> asyncResult();

		@SuppressWarnings("rawtypes")
		AsyncResult dumbAsyncResult();

		String string();
	}

}
