package com.github.ljtfreitas.restify.http.client.message;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CookiesTest {

	@Test
	public void shouldGenerateCookieHeaderContentFromCookiesCollection() {
		Cookies cookies = new Cookies();
		cookies.add("JSESSIONID", "abc1234def5678gh90");
		cookies.add("csrftoken", "xyz321");

		String content = cookies.toString();

		assertEquals("JSESSIONID=abc1234def5678gh90; csrftoken=xyz321", content);
	}

}
