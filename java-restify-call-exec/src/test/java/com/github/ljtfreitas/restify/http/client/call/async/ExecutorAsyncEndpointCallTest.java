package com.github.ljtfreitas.restify.http.client.call.async;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecutorAsyncEndpointCallTest {

	@Mock
	private EndpointCallCallback<String> callback;

	@Mock
	private EndpointCallSuccessCallback<String> successCallback;

	@Mock
	private EndpointCallFailureCallback failureCallback;

	@Captor
	private ArgumentCaptor<Throwable> throwableCaptor;

	private ExecutorAsyncEndpointCall<String> asyncCall;

	@Test
	public void shouldCallSuccessCallbackOnEndpointCallReturnsSomeResult() throws Exception {
		asyncCall = new ExecutorAsyncEndpointCall<>(() -> "result", r -> r.run());

		asyncCall.executeAsync(callback);

		verify(callback).onSuccess("result");
	}

	@Test
	public void shouldCallFailureCallbackWhenEndpointCallThrowException() throws Exception {
		RuntimeException exception = new RuntimeException("some exception");

		asyncCall = new ExecutorAsyncEndpointCall<>(() -> {throw exception;}, r -> r.run());

		asyncCall.executeAsync(callback);

		verify(callback).onFailure(throwableCaptor.capture());

		assertSame(exception, throwableCaptor.getValue());
	}

	@Test
	public void shouldCallSuccessCallbackOnEndpointCallReturnsSomeResultWhenExecutingWithTwoCallbacks() throws Exception {
		asyncCall = new ExecutorAsyncEndpointCall<>(() -> "result", r -> r.run());

		asyncCall.executeAsync(successCallback, null);

		verify(successCallback).onSuccess("result");
	}

	@Test
	public void shouldCallFailureCallbackOnEndpointCallThrowExceptionWhenExecutingWithTwoCallbacks() throws Exception {
		RuntimeException exception = new RuntimeException("some exception");

		asyncCall = new ExecutorAsyncEndpointCall<>(() -> {throw exception;}, r -> r.run());

		asyncCall.executeAsync(null, failureCallback);

		verify(failureCallback).onFailure(throwableCaptor.capture());

		assertSame(exception, throwableCaptor.getValue());
	}

}
