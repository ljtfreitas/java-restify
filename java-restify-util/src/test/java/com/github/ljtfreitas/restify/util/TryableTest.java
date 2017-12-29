package com.github.ljtfreitas.restify.util;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TryableTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private Simple simple = new Simple();

	@Test
	public void shouldGetResultOfTryableBlock() {
		String output = Tryable.of(simple::careful);

		assertEquals("hello", output);
	}

	@Test
	public void shouldThrowExceptionWhenTryableBlockCrash() {
		expectedException.expect(RuntimeException.class);
		expectedException.expectCause(isA(UnsupportedOperationException.class));

		Tryable.of(simple::crash);
	}

	@Test
	public void shouldGetResultOfTryableBlockWithFallback() {
		String output = Tryable.or(simple::careful, "fallback");

		assertEquals("hello", output);
	}

	@Test
	public void shouldGetFallbackResultWhenTryableBlockCrash() {
		String output = Tryable.or(simple::crash, "fallback");

		assertEquals("fallback", output);
	}

	@Test
	public void shouldGetResultOfTryableBlockWithExceptionSupplier() {
		String output = Tryable.of(simple::careful, () -> new IllegalArgumentException());

		assertEquals("hello", output);
	}
	
	@Test
	public void shouldSupplyExceptionWhenTryableBlockCrash() {
		expectedException.expect(IllegalArgumentException.class);

		Tryable.of(simple::crash, () -> new IllegalArgumentException());
	}

	@Test
	public void shouldGetResultOfTryableBlockWithTransformFunction() {
		String output = Tryable.of(simple::careful, (e) -> new IllegalArgumentException(e));

		assertEquals("hello", output);
	}

	@Test
	public void shouldTransformExceptionWhenTryableBlockCrash() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectCause(isA(UnsupportedOperationException.class));

		Tryable.of(simple::crash, (e) -> new IllegalArgumentException(e));		
	}

	@Test
	public void shouldRunTryableBlock() throws Exception {
		Simple spy = spy(simple);

		Tryable.run(spy::otherCareful);

		verify(spy).otherCareful();
	}

	@Test
	public void shouldThrowExceptionWhenTryableRunnableBlockCrash() {
		expectedException.expect(RuntimeException.class);
		expectedException.expectCause(isA(UnsupportedOperationException.class));

		Tryable.run(simple::otherCrash);
	}

	@Test
	public void shouldFailSilentlyWhenTryableBlockCrashOnSilentlyMethod() throws Exception {
		Simple spy = spy(simple);

		Tryable.silently(spy::otherCrash);

		verify(spy).otherCrash();
	}
	
	private class Simple {

		String careful() throws Exception {
			return "hello";
		}

		void otherCareful() throws Exception {
		}
		
		String crash() throws Exception {
			throw new UnsupportedOperationException("ouch");
		}

		String otherCrash() throws Exception {
			throw new UnsupportedOperationException("ouch");
		}
	}
}
