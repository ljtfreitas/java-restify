package com.github.ljtfreitas.restify.util;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TryTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private Simple simple = new Simple();

	@Test
	public void shouldGetSuccess() {
		String output = Try.of(simple::careful).get();

		assertEquals("hello", output);
	}

	@Test
	public void shouldThrowExceptionOnFailure() {
		expectedException.expect(UnsupportedOperationException.class);

		Try.of(simple::crash).get();
	}

	@Test
	public void shouldGetSuccessDespisteTheFallback() {
		String output = Try.of(simple::careful).or(() -> "fallback");

		assertEquals("hello", output);
	}

	@Test
	public void shouldConvertFailureToSuccessUsingFallback() {
		String output = Try.of(simple::crash).or(() -> "fallback");

		assertEquals("fallback", output);
	}

	@Test
	public void shouldRecoverOfFailureToOtherTry() {
		String output = Try.of(simple::crash).recover(e -> Try.success("fallback")).get();

		assertEquals("fallback", output);
	}

	@Test
	public void shouldMapSuccessResult() {
		String output = Try.of(simple::careful).map(String::toUpperCase).get();

		assertEquals("HELLO", output);
	}

	@Test
	public void shouldFlatMapSuccessResultToOtherTry() {
		String output = Try.of(simple::careful).flatMap(value -> Try.success("flattern " + value)).get();

		assertEquals("flattern hello", output);
	}

	@Test
	public void shouldConvertFailureToOtherException() {
		expectedException.expect(RuntimeException.class);
		expectedException.expectCause(isA(UnsupportedOperationException.class));
		expectedException.expectMessage("Wrapped exception");

		Try.of(simple::crash).error(e -> new RuntimeException("Wrapped exception", e)).get();
	}

	private class Simple {

		String careful() throws Exception {
			return "hello";
		}

		String crash() throws Exception {
			throw new UnsupportedOperationException("ouch");
		}
	}
}
