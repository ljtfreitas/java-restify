package com.github.ljtfreitas.restify.http.spring.client.call.exec.async;

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
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.web.context.request.async.DeferredResult;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;
import com.github.ljtfreitas.restify.http.spring.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class DeferredResultEndpointCallExecutableFactoryTest {

	@Mock
	private AsyncEndpointCallExecutable<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private DeferredResultEndpointCallExecutableFactory<String, String> factory;

	@Before
	public void setup() {
		factory = new DeferredResultEndpointCallExecutableFactory<>(new SyncTaskExecutor());

		when(delegate.execute(any(), anyVararg()))
			.then((invocation) -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsDeferredResult() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("deferredResult"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsDeferredResult() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfDeferredResult() throws Exception {
		assertEquals(JavaType.of(String.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("deferredResult"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenDeferredResultIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbDeferredResult"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithDeferredResultReturnType() throws Exception {
		AsyncEndpointCallExecutable<DeferredResult<String>, String> executable = factory
				.createAsync(new SimpleEndpointMethod(SomeType.class.getMethod("deferredResult")), delegate);

		String result = "deferred result";

		when(asyncEndpointCall.executeAsync()).thenReturn(CompletableFuture.completedFuture(result));

		DeferredResult<String> deferredResult = executable.executeAsync(asyncEndpointCall, null);

		assertEquals(result, deferredResult.getResult());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		DeferredResult<String> deferredResult();

		@SuppressWarnings("rawtypes")
		DeferredResult dumbDeferredResult();

		String string();
	}

}
