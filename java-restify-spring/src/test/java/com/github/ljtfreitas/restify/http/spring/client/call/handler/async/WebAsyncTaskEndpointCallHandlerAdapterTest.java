package com.github.ljtfreitas.restify.http.spring.client.call.handler.async;

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
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.spring.client.call.handler.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class WebAsyncTaskEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	private WebAsyncTaskEndpointCallHandlerAdapter<String, String> adapter;

	@Before
	public void setup() {
		adapter = new WebAsyncTaskEndpointCallHandlerAdapter<>();

		when(delegate.handle(any(), anyVararg()))
			.then((invocation) -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsWebAsyncTask() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("webAsyncTask"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsWebAsyncTask() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfWebAsyncTask() throws Exception {
		assertEquals(JavaType.of(String.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("webAsyncTask"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenWebAsyncTaskIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbWebAsyncTask"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithWebAsyncTaskReturnType() throws Exception {
		EndpointCallHandler<WebAsyncTask<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("webAsyncTask")), delegate);

		String result = "future result";

		WebAsyncTask<String> future = handler.handle(() -> result, null);

		assertNotNull(future);
		assertEquals(result, future.getCallable().call());

		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(any(), anyVararg());
	}

	interface SomeType {

		WebAsyncTask<String> webAsyncTask();

		@SuppressWarnings("rawtypes")
		WebAsyncTask dumbWebAsyncTask();

		String string();
	}
}
