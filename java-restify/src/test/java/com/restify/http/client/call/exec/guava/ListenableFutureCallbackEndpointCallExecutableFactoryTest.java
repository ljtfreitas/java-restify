package com.restify.http.client.call.exec.guava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.restify.http.contract.metadata.EndpointMethodParameters;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

public class ListenableFutureCallbackEndpointCallExecutableFactoryTest {

	private ListenableFutureCallbackEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new ListenableFutureCallbackEndpointCallExecutableFactory<>(MoreExecutors.newDirectExecutorService());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithFutureCallbackParameter() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(FutureCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("futureWithCallback", FutureCallback.class), parameters);

		assertTrue(factory.supports(endpointMethod));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodNotIsRunnableAsync() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("sync"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithFutureCallbackParameter() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(FutureCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("futureWithDumbCallbak", FutureCallback.class), parameters);

		EndpointCallExecutable<Void, String> executable = factory.create(endpointMethod);

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

		executable.execute(() -> result, new Object[]{callbackArgument});
		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointRunnableAsyncMethodHasAFutureCallbackParameterWithoutParameterizedType() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback", FutureCallback.class, EndpointMethodParameterType.ENDPOINT_CALLBACK));

		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("futureWithCallback", FutureCallback.class), parameters);

		EndpointCallExecutable<Void, String> executable = factory.create(endpointMethod);

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

		executable.execute(() -> result, new Object[]{callbackArgument});
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		void futureWithCallback(FutureCallback<String> callback);

		@SuppressWarnings("rawtypes")
		void futureWithDumbCallbak(FutureCallback callback);

		String sync();
	}
}
