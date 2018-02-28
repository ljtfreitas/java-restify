package com.github.ljtfreitas.restify.http.client.call.exec.async;

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

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
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
	private AsyncEndpointCall<String> asyncEndpointCall;

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	@Mock
	private EndpointCallSuccessCallback<String> successCallback;

	@Mock
	private EndpointCallFailureCallback failureCallback;

	@Mock
	private EndpointCallCallback<String> singleCallback;

	@Mock
	private EndpointResponseCallback<String> responseSuccessCallback;

	@Mock
	private EndpointResponseFailureCallback responseFailureCallback;

	private AsyncCallbackEndpointCallExecutableFactory<String, String> factory;

	private EndpointMethod asyncEndpointMethodWithSuccessCallback;

	private EndpointMethod asyncEndpointMethodWithFailureCallback;

	private EndpointMethod asyncEndpointMethodWithSingleCallback;

	private EndpointMethod asyncEndpointMethodWithMultiplesCallbacks;

	private EndpointMethod asyncEndpointMethodWithResponseCallback;

	private EndpointMethod asyncEndpointMethodWithResponseFailureCallback;

	private String asyncResult;

	@Before
	public void setup() throws Exception {
		factory = new AsyncCallbackEndpointCallExecutableFactory<>(r -> r.run());

		when(delegate.execute(any(), anyVararg()))
			.then((invocation) -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));

		asyncResult = "async result";

		when(asyncEndpointCall.executeAsync()).thenReturn(CompletableFuture.completedFuture(asyncResult));

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

	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithSuccessCallbackParameter() {
		AsyncEndpointCallExecutable<Void, String> executable = factory.createAsync(asyncEndpointMethodWithSuccessCallback, delegate);

		asyncResult = "async result";

		executable.executeAsync(asyncEndpointCall, new Object[] { successCallback });

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(successCallback).onSuccess(asyncResult);
	}

	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithFailureCallbackParameter() {
		AsyncEndpointCallExecutable<Void, String> executable = factory.createAsync(asyncEndpointMethodWithFailureCallback, delegate);

		RuntimeException exception = new RuntimeException("ooops");

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);
		when(asyncEndpointCall.executeAsync()).thenReturn(future);

		executable.executeAsync(asyncEndpointCall, new Object[] { failureCallback });

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(failureCallback).onFailure(exception);
	}

	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithSingleCallbackParameter() {
		AsyncEndpointCallExecutable<Void, String> executable = factory.createAsync(asyncEndpointMethodWithSingleCallback, delegate);

		executable.executeAsync(asyncEndpointCall, new Object[] { singleCallback });

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(singleCallback).onSuccess(asyncResult);
	}

	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithMultiplesCallbackParameters() {
		AsyncEndpointCallExecutable<Void, String> executable = factory.createAsync(asyncEndpointMethodWithMultiplesCallbacks, delegate);

		executable.executeAsync(asyncEndpointCall, new Object[] { successCallback, failureCallback });

		verify(successCallback).onSuccess(asyncResult);
	}

	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithResponseCallbackParameter() {
		AsyncEndpointCallExecutable<Void, String> executable = factory.createAsync(asyncEndpointMethodWithResponseCallback, delegate);

		executable.executeAsync(asyncEndpointCall, new Object[] { responseSuccessCallback });

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(responseSuccessCallback).onSuccess(asyncResult);
	}

	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithResponseFailureCallbackParameter() {
		AsyncEndpointCallExecutable<Void, String> executable = factory.createAsync(asyncEndpointMethodWithResponseFailureCallback, delegate);

		RuntimeException exception = new RuntimeException("ooops");

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);
		when(asyncEndpointCall.executeAsync()).thenReturn(future);

		executable.executeAsync(asyncEndpointCall, new Object[] { responseFailureCallback });

		assertEquals(JavaType.of(String.class), executable.returnType());

		verify(responseFailureCallback).onFailure(exception);
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
}
