package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

public class OAuth2AccessTokenMemoryStoreTest {

	private OAuth2AccessTokenMemoryStore acessTokenMemoryStore;

	@Test
	public void shouldSaveAccessTokenOnStore() {
		OAuth2DelegateUser user = () -> "user-identity-key";

		OAuth2Configuration configuration = new OAuth2Configuration.Builder()
				.resourceKey("my-resource-server")
				.credentials(OAuth2ClientCredentials.clientId("client-id"))
				.scopes("read", "write")
				.build();

		acessTokenMemoryStore = new OAuth2AccessTokenMemoryStore();

		assertFalse(acessTokenMemoryStore.findBy(user, configuration).isPresent());

		OAuth2AccessToken source = OAuth2AccessToken.bearer("accessToken");

		acessTokenMemoryStore.add(user, configuration, source);

		Optional<OAuth2AccessToken> foundAccessToken = acessTokenMemoryStore.findBy(user, configuration);

		assertTrue(foundAccessToken.isPresent());
		assertSame(source, foundAccessToken.get());
	}
}
