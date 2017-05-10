package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.*;

import java.security.Principal;
import java.util.Optional;

import org.junit.Test;

public class AccessTokenMemoryStoreTest {

	private AccessTokenMemoryStore acessTokenMemoryStore;

	@Test
	public void shouldSaveAccessTokenOnStore() {
		Principal user = () -> "user-identity-key";

		OAuth2Configuration configuration = new OAuth2Configuration.Builder()
				.credentials(ClientCredentials.clientId("client-id"))
				.scopes("read", "write")
				.build();

		acessTokenMemoryStore = new AccessTokenMemoryStore();

		assertFalse(acessTokenMemoryStore.findBy(user, configuration).isPresent());

		AccessToken source = AccessToken.bearer("accessToken");

		acessTokenMemoryStore.add(user, configuration, source);

		Optional<AccessToken> foundAccessToken = acessTokenMemoryStore.findBy(user, configuration);

		assertTrue(foundAccessToken.isPresent());
		assertSame(source, foundAccessToken.get());
	}
}
