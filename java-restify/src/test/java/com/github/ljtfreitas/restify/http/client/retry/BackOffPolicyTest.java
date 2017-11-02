package com.github.ljtfreitas.restify.http.client.retry;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BackOffPolicyTest {

	@Parameters(name = "BackOff execution test ({index}): {0}")
	public static Collection<Object[]> parameters() {
		return Arrays.asList(new Object[][] {
				{ new BackOffParameters(1, BackOffPolicy.DEFAULT_BACKOFF_DELAY,
						BackOffPolicy.DEFAULT_BACKOFF_MULTIPLIER, Duration.ofMillis(1000l)) },

				{ new BackOffParameters(2, BackOffPolicy.DEFAULT_BACKOFF_DELAY,
						BackOffPolicy.DEFAULT_BACKOFF_MULTIPLIER, Duration.ofMillis(1000l)) },

				{ new BackOffParameters(3, BackOffPolicy.DEFAULT_BACKOFF_DELAY,
						BackOffPolicy.DEFAULT_BACKOFF_MULTIPLIER, Duration.ofMillis(1000l)) },

				{ new BackOffParameters(4, BackOffPolicy.DEFAULT_BACKOFF_DELAY,
						BackOffPolicy.DEFAULT_BACKOFF_MULTIPLIER, Duration.ofMillis(1000l)) },

				{ new BackOffParameters(1, 2000l, BackOffPolicy.DEFAULT_BACKOFF_MULTIPLIER, Duration.ofMillis(2000l)) },

				{ new BackOffParameters(2, 2000l, BackOffPolicy.DEFAULT_BACKOFF_MULTIPLIER, Duration.ofMillis(2000l)) },

				{ new BackOffParameters(3, 2000l, BackOffPolicy.DEFAULT_BACKOFF_MULTIPLIER, Duration.ofMillis(2000l)) },

				{ new BackOffParameters(4, 2000l, BackOffPolicy.DEFAULT_BACKOFF_MULTIPLIER, Duration.ofMillis(2000l)) },

				{ new BackOffParameters(1, BackOffPolicy.DEFAULT_BACKOFF_DELAY, 2, Duration.ofMillis(1000l)) },

				{ new BackOffParameters(2, BackOffPolicy.DEFAULT_BACKOFF_DELAY, 2, Duration.ofMillis(2000l)) },

				{ new BackOffParameters(3, BackOffPolicy.DEFAULT_BACKOFF_DELAY, 2, Duration.ofMillis(4000l)) },

				{ new BackOffParameters(4, BackOffPolicy.DEFAULT_BACKOFF_DELAY, 2, Duration.ofMillis(8000l)) },

				{ new BackOffParameters(1, BackOffPolicy.DEFAULT_BACKOFF_DELAY, 2, Duration.ofMillis(1000l)) },

				{ new BackOffParameters(2, BackOffPolicy.DEFAULT_BACKOFF_DELAY, 1.5, Duration.ofMillis(1500l)) },

				{ new BackOffParameters(3, BackOffPolicy.DEFAULT_BACKOFF_DELAY, 1.5, Duration.ofMillis(2250l)) },

				{ new BackOffParameters(4, BackOffPolicy.DEFAULT_BACKOFF_DELAY, 1.5, Duration.ofMillis(3375l)) },

				{ new BackOffParameters(1, 2000l, 1.5, Duration.ofMillis(2000l)) },

				{ new BackOffParameters(2, 2000l, 1.5, Duration.ofMillis(3000l)) },

				{ new BackOffParameters(3, 2000l, 1.5, Duration.ofMillis(4500l)) },

				{ new BackOffParameters(4, 2000l, 1.5, Duration.ofMillis(6750l)) },

				{ new BackOffParameters(5, 2000l, 1.5, Duration.ofMillis(10125)) } });
	}

	private final BackOffParameters parameters;

	public BackOffPolicyTest(BackOffParameters parameters) {
		this.parameters = parameters;
	}

	@Test
	public void test() {
		BackOffPolicy policy = new BackOffPolicy(parameters.delay, parameters.multiplier);

		assertEquals(parameters.expected, policy.backOff(parameters.attempt));
	}

	private static class BackOffParameters {

		private final int attempt;
		private final long delay;
		private final double multiplier;
		private final Duration expected;

		private BackOffParameters(int attempt, long delay, double multiplier, Duration expected) {
			this.attempt = attempt;
			this.delay = delay;
			this.multiplier = multiplier;
			this.expected = expected;
		}

		@Override
		public String toString() {
			return "Attempt: " + attempt +
					", with delay: " + delay +
					" and multiplier: " + multiplier +
					". Duration expected is :" + expected;
		}
	}
}
