package com.github.ljtfreitas.restify.http.client.hateoas;

import static org.junit.Assert.*;

import org.junit.Test;

public class LinkTest {

	@Test
	public void shouldFollowLink() {
		Link link = Link.self("http://my.api.com/me");

		Object response = link.follow();

		assertNotNull(response);
	}

}
