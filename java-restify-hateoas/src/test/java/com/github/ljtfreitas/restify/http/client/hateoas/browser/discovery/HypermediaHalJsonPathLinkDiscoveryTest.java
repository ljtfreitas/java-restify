package com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.message.ContentType;

public class HypermediaHalJsonPathLinkDiscoveryTest {

	private HypermediaHalJsonPathLinkDiscovery discovery;

	private RawResource resource;

	@Before
	public void setup() {
		discovery = new HypermediaHalJsonPathLinkDiscovery();

		String json = "{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\","
					+ "\"_links\":{"
						+ "\"self\":{\"href\":\"http://localhost:8080/\"},"
						+ "\"friends\":{\"href\":\"http://localhost:8080/tiago/friends\",\"templated\":true}"
					+ "}}";

		resource = RawResource.of(json, ContentType.of("application/json"));
	}

	@Test
	public void shouldExtractLinkByName() {
		Optional<Link> selfLink = discovery.find("self", resource);

		assertTrue(selfLink.isPresent());
		assertEquals("self", selfLink.get().rel());
		assertEquals("http://localhost:8080/", selfLink.get().href());
	}

	@Test
	public void shouldExtractLinkUsingJsonPathExpression() {
		Optional<Link> friendsLink = discovery.find("$._links.friends.href", resource);

		assertTrue(friendsLink.isPresent());
		assertEquals("http://localhost:8080/tiago/friends", friendsLink.get().href());
	}
}
