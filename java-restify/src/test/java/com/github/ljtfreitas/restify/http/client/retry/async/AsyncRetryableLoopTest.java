package com.github.ljtfreitas.restify.http.client.retry.async;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseGatewayTimeoutException;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseInternalServerErrorException;
import com.github.ljtfreitas.restify.http.client.retry.BackOffPolicy;
import com.github.ljtfreitas.restify.http.client.retry.RetryConditionMatcher;
import com.github.ljtfreitas.restify.http.client.retry.RetryConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class AsyncRetryableLoopTest {

	@Mock
	private MyObject myObject;

	@Mock
	private BackOffPolicy backOffPolicy;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private AsyncRetryableLoop asyncRetryableLoop;

	@Before
	public void setup() {
		RetryConfiguration configuration = new RetryConfiguration.Builder()
				.when(MyException.class)
				.when(HttpStatusCode.INTERNAL_SERVER_ERROR, HttpStatusCode.GATEWAY_TIMEOUT)
					.build();

		when(backOffPolicy.backOff(anyInt())).thenReturn(Duration.ZERO);

		asyncRetryableLoop = new AsyncRetryableLoop(Executors.newSingleThreadScheduledExecutor(), new RetryConditionMatcher(configuration.conditions()),
				backOffPolicy);
	}

	@Test
	public void shouldRepeatRetryableBlockUntilTheLimitOfAttempts() throws Exception {
		when(myObject.async())
			.thenReturn(exceptionally(new MyException("1st error...")))
			.thenReturn(exceptionally(new MyException("1st error...")))
			.thenReturn(completed("success"));

		CompletableFuture<String> future = asyncRetryableLoop.repeat(3, myObject::async);

		String output = future.get();

		assertEquals("success", output);

		verify(myObject, times(3)).async();
	}

	@Test
	public void shouldRepeatRetryableBlockWhenTheResponseExceptionIsARetryableStatusCode() throws Exception {
		when(myObject.async())
			.thenReturn(exceptionally(new EndpointResponseInternalServerErrorException("Buuuuuu", Headers.empty(), "server error...")))
			.thenReturn(exceptionally(new EndpointResponseGatewayTimeoutException("Buuuuuu", Headers.empty(), "gateway timeout...")))
			.thenReturn(completed("success"));

		CompletableFuture<String> future = asyncRetryableLoop.repeat(3, myObject::async);

		String output = future.get();

		assertEquals("success", output);

		verify(myObject, times(3)).async();
	}

	@Test
	public void shouldThrowLastExceptionWhenTheLimitOfAttemptsAreReached() throws Exception {
		MyException expected = new MyException("3st error...");

		when(myObject.async())
			.thenReturn(exceptionally(new MyException("1st error...")))
			.thenReturn(exceptionally(new MyException("2st error...")))
			.thenReturn(exceptionally(expected));

		expectedException.expectCause(deepCause(expected));

		CompletableFuture<String> future = asyncRetryableLoop.repeat(3, myObject::async);

		Thread.sleep(1000);

		verify(myObject, times(3)).async();

		future.get();
	}

	@Test
	public void shouldThrowOriginalExceptionWhenTheNumberOfAttemptsIsOne() throws Exception {
		MyException expected = new MyException("1st and unique error...");

		when(myObject.async()).thenReturn(exceptionally(expected));

		expectedException.expectCause(deepCause(expected));

		CompletableFuture<String> future = asyncRetryableLoop.repeat(1, myObject::async);

		Thread.sleep(1000);

		verify(myObject).async();

		future.get();
	}

	private <T> CompletableFuture<T> exceptionally(Exception e) {
		CompletableFuture<T> future = new CompletableFuture<>();
		future.completeExceptionally(e);
		return future;
	}

	private <T> CompletableFuture<T> completed(T value) {
		return CompletableFuture.completedFuture(value);
	}

	private Matcher<? extends Throwable> deepCause(Exception expectedCause) {
		return new BaseMatcher<Throwable>() {

			@Override
			public boolean matches(Object argument) {
				Throwable exception = (Throwable) argument;
				Throwable cause = exception.getCause();

				while (cause != null) {
					if (expectedCause.equals(cause)) return true;
					cause = cause.getCause();
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText(expectedCause.getClass().getName());
			}

		};
	}

	private interface MyObject {
		CompletableFuture<String> async();
	}

	@SuppressWarnings("serial")
	private class MyException extends RuntimeException {

		public MyException(String message) {
			super(message);
		}
	}
}
