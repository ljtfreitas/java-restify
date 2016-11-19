package com.restify.http.contract;

import static org.junit.Assert.*;

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
