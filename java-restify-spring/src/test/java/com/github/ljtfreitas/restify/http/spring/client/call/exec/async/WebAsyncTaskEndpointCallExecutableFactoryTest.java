package com.github.ljtfreitas.restify.http.spring.client.call.exec.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
import org.springframework.web.context.request.async.WebAsyncTask;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class WebAsyncTaskEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private WebAsyncTaskEndpointCallExecutableFactory<String, String> factory;

	@Before
	public void setup() {
		factory = new WebAsyncTaskEndpointCallExecutableFactory<>();

		when(delegate.execute(any(), anyVararg()))
			.then((invocation) -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsWebAsyncTask() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("webAsyncTask"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsWebAsyncTask() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfWebAsyncTask() throws Exception {
		assertEquals(JavaType.of(String.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("webAsyncTask"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenWebAsyncTaskIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbWebAsyncTask"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithWebAsyncTaskReturnType() throws Exception {
		EndpointCallExecutable<WebAsyncTask<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("webAsyncTask")), delegate);

		String result = "future result";

		WebAsyncTask<String> future = executable.execute(() -> result, null);

		assertNotNull(future);
		assertEquals(result, future.getCallable().call());

		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		WebAsyncTask<String> webAsyncTask();

		@SuppressWarnings("rawtypes")
		WebAsyncTask dumbWebAsyncTask();

		String string();
	}
}
