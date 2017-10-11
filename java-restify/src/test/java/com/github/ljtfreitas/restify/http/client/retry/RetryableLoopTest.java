package com.github.ljtfreitas.restify.http.client.retry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseGatewayTimeoutException;
import com.github.ljtfreitas.restify.http.client.response.RestifyEndpointResponseInternalServerErrorException;

@RunWith(MockitoJUnitRunner.class)
public class RetryableLoopTest {

	@Mock
	private MyObject myObject;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private RetryableLoop retryLoop;

	@Before
	public void setup() {
		RetryConfiguration configuration = new RetryConfiguration.Builder()
				.when(MyException.class)
				.when(HttpStatusCode.INTERNAL_SERVER_ERROR, HttpStatusCode.GATEWAY_TIMEOUT)
					.build();

		retryLoop = new RetryableLoop(new RetryConditionMatcher(configuration));
	}

	@Test
	public void shouldRepeatRetryableBlockUntilTheLimitOfAttempts() {
		when(myObject.bla())
			.thenThrow(new MyException("1st error..."))
			.thenThrow(new MyException("2st error..."))
			.thenReturn("success");

		String output = retryLoop.repeat(3, myObject::bla);

		assertEquals("success", output);

		verify(myObject, times(3)).bla();
	}

	@Test
	public void shouldRepeatRetryableBlockWhenTheResponseExceptionIsARetryableStatusCode() {
		when(myObject.bla())
			.thenThrow(new RestifyEndpointResponseInternalServerErrorException("Buuuuuu", Headers.empty(), "server error..."))
			.thenThrow(new RestifyEndpointResponseGatewayTimeoutException("Buuuuuu", Headers.empty(), "gateway timeout..."))
			.thenReturn("success");

		String output = retryLoop.repeat(3, myObject::bla);

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

		retryLoop.repeat(3, myObject::bla);
	}

	@Test
	public void shouldThrowOriginalExceptionWhenTheNumberOfAttemptsIsOne() {
		expectedException.expect(MyException.class);
		expectedException.expectMessage("1st and unique error...");

		when(myObject.bla())
			.thenThrow(new MyException("1st and unique error..."));

		retryLoop.repeat(1, myObject::bla);
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
