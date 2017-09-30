package com.github.ljtfreitas.restify.http.client.retry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RetryLoopTest {

	@Mock
	private MyObject myObject;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private RetryLoop retryLoop;

	@Before
	public void setup() {
		retryLoop = new RetryLoop(3, Arrays.asList(MyException.class));
	}

	@Test
	public void shouldRepeatRetryableBlockUntilTheLimitOfAttempts() {
		when(myObject.bla())
			.thenThrow(new MyException("1st error..."))
			.thenThrow(new MyException("2st error..."))
			.thenReturn("success");

		String output = retryLoop.repeat(myObject::bla);

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

		retryLoop.repeat(myObject::bla);
	}

	@Test
	public void shouldThrowOriginalExceptionWhenTheNumberOfAttemptsIsOne() {
		retryLoop = new RetryLoop(1, Arrays.asList(MyException.class));

		expectedException.expect(MyException.class);
		expectedException.expectMessage("1st and unique error...");

		when(myObject.bla())
			.thenThrow(new MyException("1st and unique error..."));

		retryLoop.repeat(myObject::bla);
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
