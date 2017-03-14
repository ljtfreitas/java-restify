package com.github.ljtfreitas.restify.http.client.call.async;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.async.DefaultAsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.request.async.EndpointCallCallback;
import com.github.ljtfreitas.restify.http.client.request.async.EndpointCallFailureCallback;
import com.github.ljtfreitas.restify.http.client.request.async.EndpointCallSuccessCallback;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAsyncEndpointCallTest {

	@Mock
	private EndpointCallCallback<String> callback;

	@Mock
	private EndpointCallSuccessCallback<String> successCallback;

	@Mock
	private EndpointCallFailureCallback failureCallback;

	@Captor
	private ArgumentCaptor<Throwable> throwableCaptor;

	private DefaultAsyncEndpointCall<String> asyncCall;

	@Test
	public void shouldCallSuccessCallbackOnEndpointCallReturnsSomeResult() throws Exception {
		asyncCall = new DefaultAsyncEndpointCall<>(r -> r.run(), () -> "result");

		asyncCall.execute(callback);

		verify(callback).onSuccess("result");
	}

	@Test
	public void shouldCallFailureCallbackWhenEndpointCallThrowException() throws Exception {
		RuntimeException exception = new RuntimeException("some exception");

		asyncCall = new DefaultAsyncEndpointCall<>(r -> r.run(), () -> {throw exception;});

		asyncCall.execute(callback);

		verify(callback).onFailure(throwableCaptor.capture());

		assertSame(exception, throwableCaptor.getValue());
	}

	@Test
	public void shouldCallSuccessCallbackOnEndpointCallReturnsSomeResultWhenExecutingWithTwoCallbacks() throws Exception {
		asyncCall = new DefaultAsyncEndpointCall<>(r -> r.run(), () -> "result");

		asyncCall.execute(successCallback, null);

		verify(successCallback).onSuccess("result");
	}

	@Test
	public void shouldCallFailureCallbackOnEndpointCallThrowExceptionWhenExecutingWithTwoCallbacks() throws Exception {
		RuntimeException exception = new RuntimeException("some exception");

		asyncCall = new DefaultAsyncEndpointCall<>(r -> r.run(), () -> {throw exception;});

		asyncCall.execute(null, failureCallback);

		verify(failureCallback).onFailure(throwableCaptor.capture());

		assertSame(exception, throwableCaptor.getValue());
	}

}
