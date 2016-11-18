package com.restify.http.client.call.exec.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.restify.http.contract.metadata.EndpointMethod;
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

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private AsyncCallbackEndpointCallExecutableFactory<String, String> factory;

	private EndpointMethod asyncEndpointMethodWithSingleCallback;

	private EndpointMethod asyncEndpointMethodWithMultiplesCallbacks;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception {
		factory = new AsyncCallbackEndpointCallExecutableFactory<>(r -> r.run(), asyncEndpointCallFactoryMock);

		when(asyncEndpointCallFactoryMock.create(notNull(EndpointCall.class), notNull(Executor.class)))
			.thenReturn(asyncEndpointCall);

		when(delegate.execute(any(), anyVararg()))
			.then((invocation) -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));

		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(EndpointCallCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		asyncEndpointMethodWithSingleCallback = new SimpleEndpointMethod(SomeType.class.getMethod("asyncWithSingleCallback",
				EndpointCallCallback.class), parameters);

		parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "successCallback",
				new SimpleParameterizedType(EndpointCallSuccessCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));
		parameters.put(new EndpointMethodParameter(1, "failureCallback", EndpointCallFailureCallback.class, EndpointMethodParameterType.ENDPOINT_CALLBACK));

		asyncEndpointMethodWithMultiplesCallbacks = new SimpleEndpointMethod(SomeType.class.getMethod("asyncWithMultiplesCallbacks",
				EndpointCallSuccessCallback.class, EndpointCallFailureCallback.class), parameters);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsync() throws Exception {
		assertTrue(factory.supports(asyncEndpointMethodWithSingleCallback));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodIsNotRunnableAsync() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("sync"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfEndpointCallSuccessCallbackParameter() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(asyncEndpointMethodWithSingleCallback));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithSingleCallbackParameter() throws Exception {
		EndpointCallExecutable<Void, String> executable = factory.create(asyncEndpointMethodWithSingleCallback, delegate);

		String result = "async result";
		SimpleEndpointCallCallback callbackArgument = new SimpleEndpointCallCallback();

		executable.execute(() -> result, new Object[]{callbackArgument});

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(asyncEndpointCallFactoryMock).create(notNull(EndpointCall.class), notNull(Executor.class));
		verify(asyncEndpointCall).execute(callbackArgument, callbackArgument);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithMultiplesCallbackParameters() throws Exception {
		EndpointCallExecutable<Void, String> executable = factory.create(asyncEndpointMethodWithMultiplesCallbacks, delegate);

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
