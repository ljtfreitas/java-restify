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

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CompletableFutureAsyncEndpointCallTest {

	@Mock
	private EndpointCallCallback<String> callback;

	@Mock
	private EndpointCallSuccessCallback<String> successCallback;

	@Mock
	private EndpointCallFailureCallback failureCallback;

	private CompletableFutureAsyncEndpointCall<String> asyncCall;

	private String response;

	@Before
	public void setup() {
		response = "async result";

		CompletableFuture<String> future = CompletableFuture.completedFuture(response);

		asyncCall = new CompletableFutureAsyncEndpointCall<>(future, r -> r.run());
	}

	@Test
	public void shouldExecuteAsyncWithSingleCallback() {
		asyncCall.executeAsync(callback);

		verify(callback).onSuccess(response);
	}

	@Test
	public void shouldExecuteAsyncWithMultipleCallbacks() {
		asyncCall.executeAsync(successCallback, failureCallback);

		verify(successCallback).onSuccess(response);
	}

	@Test
	public void shouldCallFailureCallbackWhenFutureThrowException() {
		RuntimeException exception = new RuntimeException("oops");

		CompletableFuture<String> future = new CompletableFuture<>();
		future.completeExceptionally(exception);

		asyncCall = new CompletableFutureAsyncEndpointCall<>(future, r -> r.run());

		asyncCall.executeAsync(successCallback, failureCallback);

		verify(failureCallback).onFailure(exception);
	}

	@Test
	public void shouldExecuteAsyncWithCompletableFuture() throws Exception {
		CompletableFuture<String> future = asyncCall.executeAsync();

		assertSame(response, future.get());
	}

	@Test
	public void shouldExecuteSync() throws Exception {
		String output = asyncCall.execute();

		assertSame(response, output);
	}
}
