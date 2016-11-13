package com.restify.http.client.call.exec.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.async.AsyncEndpointCall;
import com.restify.http.client.call.async.AsyncEndpointCallFactory;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.request.async.EndpointCallCallback;
import com.restify.http.client.request.async.EndpointCallFailureCallback;
import com.restify.http.client.request.async.EndpointCallSuccessCallback;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.restify.http.contract.metadata.EndpointMethodParameters;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class AsyncCallbackEndpointCallExecutableFactoryTest {

	@Mock
	private AsyncEndpointCallFactory asyncEndpointCallFactoryMock;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	private AsyncCallbackEndpointCallExecutableFactory<String> factory;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		factory = new AsyncCallbackEndpointCallExecutableFactory<>(r -> r.run(), asyncEndpointCallFactoryMock);

		when(asyncEndpointCallFactoryMock.create(notNull(EndpointCall.class), notNull(Executor.class)))
			.thenReturn(asyncEndpointCall);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsync() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(EndpointCallCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("asyncWithSingleCallback", EndpointCallCallback.class), parameters);

		assertTrue(factory.supports(endpointMethod));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodNotIsRunnableAsync() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("sync"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithSingleCallbackParameter() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(EndpointCallCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("asyncWithSingleCallback", EndpointCallCallback.class), parameters);

		EndpointCallExecutable<Void, String> executable = factory.create(endpointMethod);

		String result = "async result";
		SimpleEndpointCallCallback callbackArgument = new SimpleEndpointCallCallback();

		executable.execute(() -> result, new Object[]{callbackArgument});

		verify(asyncEndpointCallFactoryMock).create(notNull(EndpointCall.class), notNull(Executor.class));
		verify(asyncEndpointCall).execute(callbackArgument, callbackArgument);

		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithMultiplesCallbackParameters() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();

		parameters.put(new EndpointMethodParameter(0, "successCallback",
				new SimpleParameterizedType(EndpointCallSuccessCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		parameters.put(new EndpointMethodParameter(1, "failureCallback", EndpointCallFailureCallback.class, EndpointMethodParameterType.ENDPOINT_CALLBACK));

		Method javaMethod = SomeType.class.getMethod("asyncWithMultiplesCallbacks", EndpointCallSuccessCallback.class, EndpointCallFailureCallback.class);

		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(javaMethod, parameters);

		EndpointCallExecutable<Void, String> executable = factory.create(endpointMethod);

		String result = "async result";
		SimpleEndpointCallSuccessCallback successCallbackArgument = new SimpleEndpointCallSuccessCallback();
		SimpleEndpointCallFailureCallback failureCallbackArgument = new SimpleEndpointCallFailureCallback();

		executable.execute(() -> result, new Object[]{successCallbackArgument, failureCallbackArgument});

		verify(asyncEndpointCallFactoryMock).create(notNull(EndpointCall.class), notNull(Executor.class));
		verify(asyncEndpointCall).execute(successCallbackArgument, failureCallbackArgument);

		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	interface SomeType {

		void asyncWithSingleCallback(EndpointCallCallback<String> callback);

		void asyncWithMultiplesCallbacks(EndpointCallSuccessCallback<String> successCallback, EndpointCallFailureCallback failureCallback);

		String sync();
	}

	private class SimpleEndpointCallCallback implements EndpointCallCallback<String> {
		@Override
		public void onSuccess(String response) {
		}

		@Override
		public void onFailure(Throwable throwable) {
		}
	}

	private class SimpleEndpointCallSuccessCallback implements EndpointCallSuccessCallback<String> {
		@Override
		public void onSuccess(String response) {
		}
	}

	private class SimpleEndpointCallFailureCallback implements EndpointCallFailureCallback {
		@Override
		public void onFailure(Throwable throwable) {
		}
	}
}
