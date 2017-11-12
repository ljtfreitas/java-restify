package com.github.ljtfreitas.restify.http.client.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ETagTest {

	@Test
	public void shouldCreateETagFromRawValue() {
		ETag eTag = ETag.of("abc1234");

		assertEquals("abc1234", eTag.raw());
		assertEquals("\"abc1234\"", eTag.toString());
	}

	@Test
	public void shouldETagBeEqualsOnlyTagIsExactlyEqual() {
		ETag eTag = ETag.of("abc1234");

		assertTrue(eTag.equals(ETag.of("abc1234")));
		assertFalse(eTag.equals(ETag.of("ABC1234")));
	}
	
	@Test
	public void shouldCreateWeakETagFromRawValue() {
		ETag eTag = ETag.weak("abc1234");

		assertEquals("abc1234", eTag.raw());
		assertEquals("W/\"abc1234\"", eTag.toString());
	}
	
	@Test
	public void shouldWeakETagBeEqualsEvenIfItSemanticallyEqual() {
		ETag eTag = ETag.weak("abc1234");

		assertTrue(eTag.equals(ETag.of("abc1234")));
		assertTrue(eTag.equals(ETag.of("ABC1234")));
		assertFalse(eTag.equals(ETag.of("1234abc")));
	}
	
	@Test
	public void shouldCreateETagToAnyResource() {
		ETag eTag = ETag.any();

		assertEquals("*", eTag.raw());
		assertEquals("*", eTag.toString());
	}

	@Test
	public void shouldETagBeEqualsToAnyOtherETag() {
		ETag eTag = ETag.any();

		assertTrue(eTag.equals(ETag.of("xyz098")));
		assertTrue(eTag.equals(ETag.of("abc1234")));
	}
}
