package com.github.ljtfreitas.restify.http.client.retry;

import static org.junit.Assert.*;

import java.time.Duration;

import org.junit.Test;

public class TimeoutRetryPolicyTest {

	@Test
	public void shouldBeRetryableBeforeTimeoutIsReached() {
		TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy(Duration.ofMillis(2000));
		assertTrue(retryPolicy.retryable());
	}

	@Test
	public void shouldNotBeRetryableAfterTimeoutIsReached() throws Exception {
		TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy(Duration.ofMillis(2000));

		Thread.sleep(4000);

		assertFalse(retryPolicy.retryable());
	}
}
