package com.github.ljtfreitas.restify.http.client.call.exec.async;

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

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCallFactory;
import com.github.ljtfreitas.restify.http.client.call.async.EndpointCallCallback;
import com.github.ljtfreitas.restify.http.client.call.async.EndpointCallFailureCallback;
import com.github.ljtfreitas.restify.http.client.call.async.EndpointCallSuccessCallback;
import com.github.ljtfreitas.restify.http.client.call.async.EndpointResponseCallback;
import com.github.ljtfreitas.restify.http.client.call.async.EndpointResponseFailureCallback;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class AsyncCallbackEndpointCallExecutableFactoryTest {

	@Mock
	private AsyncEndpointCallFactory asyncEndpointCallFactoryMock;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private AsyncCallbackEndpointCallExecutableFactory<String, String> factory;

	private EndpointMethod asyncEndpointMethodWithSuccessCallback;

	private EndpointMethod asyncEndpointMethodWithFailureCallback;

	private EndpointMethod asyncEndpointMethodWithSingleCallback;

	private EndpointMethod asyncEndpointMethodWithMultiplesCallbacks;

	private EndpointMethod asyncEndpointMethodWithResponseCallback;

	private EndpointMethod asyncEndpointMethodWithResponseFailureCallback;

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

		asyncEndpointMethodWithSuccessCallback = createEndpointMethodWithSuccessCallback();
		asyncEndpointMethodWithFailureCallback = createEndpointMethodWithFailureCallback();

		asyncEndpointMethodWithSingleCallback = createEndpointMethodWithSingleCallback();
		asyncEndpointMethodWithMultiplesCallbacks = createEndpointMethodWithMultiplesCallbacks();

		asyncEndpointMethodWithResponseCallback = createEndpointMethodWithResponseCallback();
		asyncEndpointMethodWithResponseFailureCallback = createEndpointMethodWithResponseFailureCallback();
	}

	private EndpointMethod createEndpointMethodWithSuccessCallback() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(EndpointCallSuccessCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		return new SimpleEndpointMethod(SomeType.class.getMethod("asyncWithSuccessCallback", EndpointCallSuccessCallback.class), parameters);
	}

	private EndpointMethod createEndpointMethodWithFailureCallback() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback", EndpointCallFailureCallback.class, EndpointMethodParameterType.ENDPOINT_CALLBACK));

		return new SimpleEndpointMethod(SomeType.class.getMethod("asyncWithFailureCallback", EndpointCallFailureCallback.class), parameters);
	}

	private EndpointMethod createEndpointMethodWithSingleCallback() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(EndpointCallCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		return new SimpleEndpointMethod(SomeType.class.getMethod("asyncWithSingleCallback", EndpointCallCallback.class), parameters);
	}

	private EndpointMethod createEndpointMethodWithMultiplesCallbacks() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "successCallback",
				new SimpleParameterizedType(EndpointCallSuccessCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));
		parameters.put(new EndpointMethodParameter(1, "failureCallback", EndpointCallFailureCallback.class, EndpointMethodParameterType.ENDPOINT_CALLBACK));

		return new SimpleEndpointMethod(SomeType.class.getMethod("asyncWithMultiplesCallbacks",
				EndpointCallSuccessCallback.class, EndpointCallFailureCallback.class), parameters);
	}

	private EndpointMethod createEndpointMethodWithResponseCallback() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(EndpointResponseCallback.class, null, String.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		return new SimpleEndpointMethod(SomeType.class.getMethod("asyncWithEndpointResponseCallback", EndpointResponseCallback.class), parameters);
	}

	private EndpointMethod createEndpointMethodWithResponseFailureCallback() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback", EndpointResponseFailureCallback.class, EndpointMethodParameterType.ENDPOINT_CALLBACK));

		return new SimpleEndpointMethod(SomeType.class.getMethod("asyncWithEndpointResponseFailureCallback", EndpointResponseFailureCallback.class), parameters);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithSuccessCallbackParametter() {
		assertTrue(factory.supports(asyncEndpointMethodWithSuccessCallback));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithFailureCallbackParameter() {
		assertTrue(factory.supports(asyncEndpointMethodWithFailureCallback));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithGenericCallbackParameter() {
		assertTrue(factory.supports(asyncEndpointMethodWithSingleCallback));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithMultiplesCallbackParameters() {
		assertTrue(factory.supports(asyncEndpointMethodWithMultiplesCallbacks));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsync() {
		assertTrue(factory.supports(asyncEndpointMethodWithSingleCallback));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodIsNotRunnableAsync() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("sync"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfEndpointCallSuccessCallbackParameter() {
		assertEquals(JavaType.of(String.class), factory.returnType(asyncEndpointMethodWithSuccessCallback));
	}

	@Test
	public void shouldReturnArgumentTypeOfEndpointCallCallbackParameter() {
		assertEquals(JavaType.of(String.class), factory.returnType(asyncEndpointMethodWithSingleCallback));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithSuccessCallbackParameter() {
		EndpointCallExecutable<Void, String> executable = factory.create(asyncEndpointMethodWithSuccessCallback, delegate);

		String result = "async result";
		SimpleEndpointCallSuccessCallback callbackArgument = new SimpleEndpointCallSuccessCallback();

		executable.execute(() -> result, new Object[] { callbackArgument });

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(asyncEndpointCallFactoryMock).create(notNull(EndpointCall.class), notNull(Executor.class));
		verify(asyncEndpointCall).execute(callbackArgument, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithFailureCallbackParameter() {
		EndpointCallExecutable<Void, String> executable = factory.create(asyncEndpointMethodWithFailureCallback, delegate);

		String result = "async result";
		SimpleEndpointCallFailureCallback callbackArgument = new SimpleEndpointCallFailureCallback();

		executable.execute(() -> result, new Object[]{callbackArgument});

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(asyncEndpointCallFactoryMock).create(notNull(EndpointCall.class), notNull(Executor.class));
		verify(asyncEndpointCall).execute(null, callbackArgument);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithSingleCallbackParameter() {
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
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithMultiplesCallbackParameters() {
		EndpointCallExecutable<Void, String> executable = factory.create(asyncEndpointMethodWithMultiplesCallbacks, delegate);

		String result = "async result";
		SimpleEndpointCallSuccessCallback successCallbackArgument = new SimpleEndpointCallSuccessCallback();
		SimpleEndpointCallFailureCallback failureCallbackArgument = new SimpleEndpointCallFailureCallback();

		executable.execute(() -> result, new Object[]{successCallbackArgument, failureCallbackArgument});

		verify(asyncEndpointCallFactoryMock).create(notNull(EndpointCall.class), notNull(Executor.class));
		verify(asyncEndpointCall).execute(successCallbackArgument, failureCallbackArgument);

		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithResponseCallbackParameter() {
		EndpointCallExecutable<Void, String> executable = factory.create(asyncEndpointMethodWithResponseCallback, delegate);

		String result = "async result";
		SimpleEndpointResponseCallback callbackArgument = new SimpleEndpointResponseCallback();

		executable.execute(() -> result, new Object[]{callbackArgument});

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(asyncEndpointCallFactoryMock).create(notNull(EndpointCall.class), notNull(Executor.class));
		verify(asyncEndpointCall).execute(callbackArgument, callbackArgument);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithResponseFailureCallbackParameter() {
		EndpointCallExecutable<Void, String> executable = factory.create(asyncEndpointMethodWithResponseFailureCallback, delegate);

		String result = "async result";
		EndpointResponseFailureCallback callbackArgument = new EndpointResponseFailureCallback(){};

		executable.execute(() -> result, new Object[]{callbackArgument});

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(asyncEndpointCallFactoryMock).create(notNull(EndpointCall.class), notNull(Executor.class));
		verify(asyncEndpointCall).execute(null, callbackArgument);
	}

	interface SomeType {

		void asyncWithSuccessCallback(EndpointCallSuccessCallback<String> callback);

		void asyncWithFailureCallback(EndpointCallFailureCallback callback);

		void asyncWithSingleCallback(EndpointCallCallback<String> callback);

		void asyncWithMultiplesCallbacks(EndpointCallSuccessCallback<String> successCallback, EndpointCallFailureCallback failureCallback);

		void asyncWithEndpointResponseCallback(EndpointResponseCallback<String> callback);

		void asyncWithEndpointResponseFailureCallback(EndpointResponseFailureCallback callback);

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

	private class SimpleEndpointResponseCallback extends EndpointResponseCallback<String> {
		@Override
		public void onSuccess(String response) {
		}
	}
}
