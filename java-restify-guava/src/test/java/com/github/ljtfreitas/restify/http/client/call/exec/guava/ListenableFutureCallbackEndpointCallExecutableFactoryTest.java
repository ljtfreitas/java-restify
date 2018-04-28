package com.github.ljtfreitas.restify.http.client.call.exec.guava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;

@RunWith(MockitoJUnitRunner.class)
public class ListenableFutureCallbackEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private ListenableFutureCallbackEndpointCallExecutableFactory<String, String> factory;

	private SimpleEndpointMethod futureWithCallbackEndpointMethod;

	@Before
	public void setup() throws Exception {
		factory = new ListenableFutureCallbackEndpointCallExecutableFactory<>(MoreExecutors.newDirectExecutorService());

		when(delegate.execute(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));

		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(FutureCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		futureWithCallbackEndpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("futureWithCallback", FutureCallback.class), 
				parameters);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithFutureCallbackParameter() throws Exception {
		assertTrue(factory.supports(futureWithCallbackEndpointMethod));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodIsNotRunnableAsync() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("sync"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfFutureCallbackParameter() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(futureWithCallbackEndpointMethod));
	}

	@Test
	public void shouldReturnObjectTypeWhenFutureCallbackParameterIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("futureWithDumbCallbak", FutureCallback.class))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithFutureCallbackParameter() throws Exception {
		AsyncEndpointCallExecutable<Void, String> executable = factory.createAsync(futureWithCallbackEndpointMethod, delegate);

		String result = "future result";
		FutureCallback<String> callbackArgument = new FutureCallback<String>() {

			@Override
			public void onSuccess(String callResult) {
				assertEquals(result, callResult);
			}

			@Override
			public void onFailure(Throwable t) {
				fail(t.getMessage());
			}
		};

		when(asyncEndpointCall.executeAsync())
			.thenReturn(CompletableFuture.completedFuture(result));

		executable.executeAsync(asyncEndpointCall, new Object[]{callbackArgument});

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		void futureWithCallback(FutureCallback<String> callback);

		@SuppressWarnings("rawtypes")
		void futureWithDumbCallbak(FutureCallback callback);

		String sync();
	}
}
