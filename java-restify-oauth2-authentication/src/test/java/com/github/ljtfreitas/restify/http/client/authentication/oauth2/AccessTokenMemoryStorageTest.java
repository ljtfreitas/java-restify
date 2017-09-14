package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.*;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;

public class AccessTokenMemoryStorageTest {

	private AccessTokenMemoryStorage acessTokenMemoryStore;

	@Test
	public void shouldSaveAccessTokenOnStore() {
		Principal user = () -> "user-identity-key";

		GrantProperties properties = new GrantProperties.Builder()
				.credentials(ClientCredentials.clientId("client-id"))
				.scopes("read", "write")
				.build();

		EndpointRequest source = new EndpointRequest(URI.create("http://my.resource.server/path"), "GET");

		OAuth2AuthenticatedEndpointRequest request = new OAuth2AuthenticatedEndpointRequest(source, properties, user);

		acessTokenMemoryStore = new AccessTokenMemoryStorage();

		assertFalse(acessTokenMemoryStore.findBy(request).isPresent());

		AccessToken accessToken = AccessToken.bearer("accessToken");

		acessTokenMemoryStore.add(request, accessToken);

		Optional<AccessToken> foundAccessToken = acessTokenMemoryStore.findBy(request);

		assertTrue(foundAccessToken.isPresent());
		assertSame(accessToken, foundAccessToken.get());
	}
}
