package com.github.ljtfreitas.restify.http.client.retry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

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

@RunWith(MockitoJUnitRunner.class)
public class RetryableLoopTest {

	@Mock
	private MyObject myObject;

	@Mock
	private BackOffPolicy backOffPolicy;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private RetryableLoop retryableLoop;

	@Before
	public void setup() {
		RetryConfiguration configuration = new RetryConfiguration.Builder()
				.when(MyException.class)
				.when(HttpStatusCode.INTERNAL_SERVER_ERROR, HttpStatusCode.GATEWAY_TIMEOUT)
					.build();

		when(backOffPolicy.backOff(anyInt())).thenReturn(Duration.ZERO);

		retryableLoop = new RetryableLoop(new RetryConditionMatcher(configuration.conditions()), backOffPolicy);
	}

	@Test
	public void shouldRepeatRetryableBlockUntilTheLimitOfAttempts() {
		when(myObject.bla())
			.thenThrow(new MyException("1st error..."))
			.thenThrow(new MyException("2st error..."))
			.thenReturn("success");

		String output = retryableLoop.repeat(3, myObject::bla);

		assertEquals("success", output);

		verify(myObject, times(3)).bla();
	}

	@Test
	public void shouldRepeatRetryableBlockWhenTheResponseExceptionIsARetryableStatusCode() {
		when(myObject.bla())
			.thenThrow(new EndpointResponseInternalServerErrorException("Buuuuuu", Headers.empty(), "server error..."))
			.thenThrow(new EndpointResponseGatewayTimeoutException("Buuuuuu", Headers.empty(), "gateway timeout..."))
			.thenReturn("success");

		String output = retryableLoop.repeat(3, myObject::bla);

		assertEquals("success", output);

		verify(myObject, times(3)).bla();
	}

	@Test
	public void shouldThrowLastExceptionWhenTheLimitOfAttemptsAreReached() {
		expectedException.expect(MyException.class);
		expectedException.expectMessage("3st error...");

		when(myObject.bla())
			.thenThrow(new MyException("1st error..."))
			.thenThrow(new MyException("2st error..."))
			.thenThrow(new MyException("3st error..."));

		retryableLoop.repeat(3, myObject::bla);
	}

	@Test
	public void shouldThrowOriginalExceptionWhenTheNumberOfAttemptsIsOne() {
		expectedException.expect(MyException.class);
		expectedException.expectMessage("1st and unique error...");

		when(myObject.bla())
			.thenThrow(new MyException("1st and unique error..."));

		retryableLoop.repeat(1, myObject::bla);
	}

	private interface MyObject {
		String bla();
	}

	@SuppressWarnings("serial")
	private class MyException extends RuntimeException {

		public MyException(String message) {
			super(message);
		}
	}
}
