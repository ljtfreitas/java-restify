package com.github.ljtfreitas.restify.http.client.call.handler.async;

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
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class AsyncCallbackEndpointCallHandlerAdapterTest {

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

	@Mock
	private EndpointCallHandler<String, String> delegate;

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

	private AsyncCallbackEndpointCallHandlerAdapter<String, String> adapter;

	private EndpointMethod asyncEndpointMethodWithSuccessCallback;

	private EndpointMethod asyncEndpointMethodWithFailureCallback;

	private EndpointMethod asyncEndpointMethodWithSingleCallback;

	private EndpointMethod asyncEndpointMethodWithMultiplesCallbacks;

	private EndpointMethod asyncEndpointMethodWithResponseCallback;

	private EndpointMethod asyncEndpointMethodWithResponseFailureCallback;

	private String asyncResult;

	@Before
	public void setup() throws Exception {
		adapter = new AsyncCallbackEndpointCallHandlerAdapter<>(r -> r.run());

		when(delegate.handle(any(), anyVararg()))
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
		assertTrue(adapter.supports(asyncEndpointMethodWithSuccessCallback));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithFailureCallbackParameter() {
		assertTrue(adapter.supports(asyncEndpointMethodWithFailureCallback));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithGenericCallbackParameter() {
		assertTrue(adapter.supports(asyncEndpointMethodWithSingleCallback));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithMultiplesCallbackParameters() {
		assertTrue(adapter.supports(asyncEndpointMethodWithMultiplesCallbacks));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsync() {
		assertTrue(adapter.supports(asyncEndpointMethodWithSingleCallback));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodIsNotRunnableAsync() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("sync"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfEndpointCallSuccessCallbackParameter() {
		assertEquals(JavaType.of(String.class), adapter.returnType(asyncEndpointMethodWithSuccessCallback));
	}

	@Test
	public void shouldReturnArgumentTypeOfEndpointCallCallbackParameter() {
		assertEquals(JavaType.of(String.class), adapter.returnType(asyncEndpointMethodWithSingleCallback));
	}

	@Test
	public void shouldCreateHandlerFromEndpointRunnableAsyncMethodWithSuccessCallbackParameter() {
		AsyncEndpointCallHandler<Void, String> handler = adapter.adaptAsync(asyncEndpointMethodWithSuccessCallback, delegate);

		asyncResult = "async result";

		handler.handleAsync(asyncEndpointCall, new Object[] { successCallback });

		assertEquals(JavaType.of(String.class), handler.returnType());

		verify(successCallback).onSuccess(asyncResult);
	}

	@Test
	public void shouldCreateHandlerFromEndpointRunnableAsyncMethodWithFailureCallbackParameter() {
		AsyncEndpointCallHandler<Void, String> handler = adapter.adaptAsync(asyncEndpointMethodWithFailureCallback, delegate);

		RuntimeException exception = new RuntimeException("ooops");

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);
		when(asyncEndpointCall.executeAsync()).thenReturn(future);

		handler.handleAsync(asyncEndpointCall, new Object[] { failureCallback });

		assertEquals(JavaType.of(String.class), handler.returnType());

		verify(failureCallback).onFailure(exception);
	}

	@Test
	public void shouldCreateHandlerFromEndpointRunnableAsyncMethodWithSingleCallbackParameter() {
		AsyncEndpointCallHandler<Void, String> handler = adapter.adaptAsync(asyncEndpointMethodWithSingleCallback, delegate);

		handler.handleAsync(asyncEndpointCall, new Object[] { singleCallback });

		assertEquals(JavaType.of(String.class), handler.returnType());

		verify(singleCallback).onSuccess(asyncResult);
	}

	@Test
	public void shouldCreateHandlerFromEndpointRunnableAsyncMethodWithMultiplesCallbackParameters() {
		AsyncEndpointCallHandler<Void, String> handler = adapter.adaptAsync(asyncEndpointMethodWithMultiplesCallbacks, delegate);

		handler.handleAsync(asyncEndpointCall, new Object[] { successCallback, failureCallback });

		verify(successCallback).onSuccess(asyncResult);
	}

	@Test
	public void shouldCreateHandlerFromEndpointRunnableAsyncMethodWithResponseCallbackParameter() {
		AsyncEndpointCallHandler<Void, String> handler = adapter.adaptAsync(asyncEndpointMethodWithResponseCallback, delegate);

		handler.handleAsync(asyncEndpointCall, new Object[] { responseSuccessCallback });

		assertEquals(JavaType.of(String.class), handler.returnType());

		verify(responseSuccessCallback).onSuccess(asyncResult);
	}

	@Test
	public void shouldCreateHandlerFromEndpointRunnableAsyncMethodWithResponseFailureCallbackParameter() {
		AsyncEndpointCallHandler<Void, String> handler = adapter.adaptAsync(asyncEndpointMethodWithResponseFailureCallback, delegate);

		RuntimeException exception = new RuntimeException("ooops");

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);
		when(asyncEndpointCall.executeAsync()).thenReturn(future);

		handler.handleAsync(asyncEndpointCall, new Object[] { responseFailureCallback });

		assertEquals(JavaType.of(String.class), handler.returnType());

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
