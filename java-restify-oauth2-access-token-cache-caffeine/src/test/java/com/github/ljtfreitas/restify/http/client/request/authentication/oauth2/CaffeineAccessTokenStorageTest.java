package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.security.Principal;
import java.time.Duration;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;

public class CaffeineAccessTokenStorageTest {

	private CaffeineAccessTokenStorage caffeineAccessTokenStorage;

	private AccessTokenStorageKey key;

	@Before
	public void setup() {
		Principal user = () -> "user-identity-key";

		GrantProperties properties = new GrantProperties.Builder()
				.credentials(ClientCredentials.clientId("client-id"))
				.scopes("read", "write")
				.build();

		EndpointRequest source = new EndpointRequest(URI.create("http://my.resource.server/path"), "GET");

		OAuth2AuthenticatedEndpointRequest request = new OAuth2AuthenticatedEndpointRequest(source, properties, user);

		key = AccessTokenStorageKey.by(request);

		caffeineAccessTokenStorage = new CaffeineAccessTokenStorage();
	}

	@Test
	public void shouldSaveAccessTokenOnStore() {
		assertFalse(caffeineAccessTokenStorage.findBy(key).isPresent());

		AccessToken accessToken = AccessToken.bearer("accessToken");

		caffeineAccessTokenStorage.add(key, accessToken);

		Optional<AccessToken> foundAccessToken = caffeineAccessTokenStorage.findBy(key);

		assertTrue(foundAccessToken.isPresent());
		assertEquals(accessToken, foundAccessToken.get());
	}

	@Test
	public void shouldEvictAccessTokenAfterExpirarionPeriod() throws Exception {
		assertFalse(caffeineAccessTokenStorage.findBy(key).isPresent());

		AccessToken accessToken = AccessToken.bearer("accessToken", Duration.ofSeconds(5));

		caffeineAccessTokenStorage.add(key, accessToken);

		assertTrue(caffeineAccessTokenStorage.findBy(key).isPresent());

		Thread.sleep(5500);

		assertFalse(caffeineAccessTokenStorage.findBy(key).isPresent());
	}
}
