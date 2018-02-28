/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.call.async;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseInternalServerErrorException;

@RunWith(MockitoJUnitRunner.class)
public class AsyncEndpointResponseCallTest {

	@Mock
	private AsyncEndpointRequestExecutor asyncEndpointRequestExecutor;

	@Mock
	private EndpointCallCallback<EndpointResponse<Object>> callback;

	@Mock
	private EndpointCallSuccessCallback<EndpointResponse<Object>> successCallback;

	@Mock
	private EndpointCallFailureCallback failureCallback;

	private AsyncEndpointResponseCall<Object> asyncCall;

	private EndpointResponse<Object> response;

	private EndpointRequest request;

	@Before
	public void setup() {
		request = new EndpointRequest(URI.create("http://my.api.com"), "GET");

		response = new EndpointResponse<>(StatusCode.ok(), "async result");

		CompletableFuture<EndpointResponse<Object>> completedFuture = CompletableFuture
				.completedFuture(response);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(completedFuture);

		when(asyncEndpointRequestExecutor.execute(request))
			.thenReturn(response);

		asyncCall = new AsyncEndpointResponseCall<>(request, asyncEndpointRequestExecutor, r -> r.run());
	}

	@Test
	public void shouldExecuteAsyncWithSingleCallback() {
		asyncCall.executeAsync(callback);

		verify(callback).onSuccess(response);

		verify(asyncEndpointRequestExecutor).executeAsync(request);
	}

	@Test
	public void shouldExecuteAsyncWithMultipleCallbacks() {
		asyncCall.executeAsync(successCallback, failureCallback);

		verify(successCallback).onSuccess(response);

		verify(asyncEndpointRequestExecutor).executeAsync(request);
	}

	@Test
	public void shouldCallFailureCallbackWhenExecuteAsyncCallThrowException() {
		EndpointResponseInternalServerErrorException exception = new EndpointResponseInternalServerErrorException("oops", new Headers(), "error");

		CompletableFuture<EndpointResponse<Object>> completedFuture = new CompletableFuture<>();
		completedFuture.completeExceptionally(exception);

		when(asyncEndpointRequestExecutor.executeAsync(request))
			.thenReturn(completedFuture);

		asyncCall.executeAsync(successCallback, failureCallback);

		verify(failureCallback).onFailure(exception);

		verify(asyncEndpointRequestExecutor).executeAsync(request);
	}

	@Test
	public void shouldExecuteAsyncWithCompletableFuture() throws Exception {
		CompletableFuture<EndpointResponse<Object>> future = asyncCall.executeAsync();

		assertSame(response, future.get());

		verify(asyncEndpointRequestExecutor).executeAsync(request);
	}

	@Test
	public void shouldExecuteSyncUsingEndpointResponseCall() throws Exception {
		EndpointResponse<Object> output = asyncCall.execute();

		assertSame(response, output);

		verify(asyncEndpointRequestExecutor).execute(request);
	}
}
