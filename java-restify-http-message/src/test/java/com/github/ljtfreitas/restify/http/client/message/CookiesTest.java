package com.github.ljtfreitas.restify.http.client.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

public class CookiesTest {

	@Test
	public void shouldGenerateCookieHeaderContentFromCookiesCollection() {
		Cookies cookies = new Cookies()
				.add("JSESSIONID", "abc1234def5678gh90")
				.add("csrftoken", "xyz321");

		String content = cookies.toString();

		assertEquals("JSESSIONID=abc1234def5678gh90; csrftoken=xyz321", content);
	}

	@Test
	public void shouldGenerateCookieHeaderContentFromString() {
		String cookiesAsString = "JSESSIONID=abc1234def5678gh90; csrftoken=xyz321";

		Cookies cookies = Cookies.of(cookiesAsString);

		String content = cookies.toString();

		assertEquals(cookiesAsString, content);

		Optional<Cookie> jSessionId = cookies.get("JSESSIONID");

		assertTrue(jSessionId.isPresent());
		assertEquals("abc1234def5678gh90", jSessionId.get().value());
	}
}
