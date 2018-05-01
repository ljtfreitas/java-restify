package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import static org.junit.Assert.*;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenMemoryStorage;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;

public class AccessTokenMemoryStorageTest {

	private AccessTokenStorageKey key;

	private AccessTokenMemoryStorage accessTokenMemoryStore;

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

		accessTokenMemoryStore = new AccessTokenMemoryStorage();
	}

	@Test
	public void shouldSaveAccessTokenOnStore() {
		assertFalse(accessTokenMemoryStore.findBy(key).isPresent());

		AccessToken accessToken = AccessToken.bearer("accessToken");

		accessTokenMemoryStore.add(key, accessToken);

		Optional<AccessToken> foundAccessToken = accessTokenMemoryStore.findBy(key);

		assertTrue(foundAccessToken.isPresent());
		assertSame(accessToken, foundAccessToken.get());
	}
}
