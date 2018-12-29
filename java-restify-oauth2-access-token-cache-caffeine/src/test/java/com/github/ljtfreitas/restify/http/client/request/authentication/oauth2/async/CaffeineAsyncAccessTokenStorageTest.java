package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.security.Principal;
import java.time.Duration;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenStorageKey;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;
import com.github.ljtfreitas.restify.util.Try;

public class CaffeineAsyncAccessTokenStorageTest {

	private CaffeineAsyncAccessTokenStorage caffeineAsyncAccessTokenStorage;

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

		caffeineAsyncAccessTokenStorage = new CaffeineAsyncAccessTokenStorage();
	}

	@Test
	public void shouldSaveAccessTokenOnStoreAsync() {
		caffeineAsyncAccessTokenStorage.findBy(key)

			.thenAccept(accessToken -> assertFalse(accessToken.isPresent()))

			.toCompletableFuture()
			.join();

		AccessToken newAccessToken = AccessToken.bearer("accessToken");

		caffeineAsyncAccessTokenStorage.add(key, newAccessToken)

			.thenCompose(a -> caffeineAsyncAccessTokenStorage.findBy(key))

			.thenAccept(foundAccessToken -> {
				assertTrue(foundAccessToken.isPresent());
				assertEquals(newAccessToken, foundAccessToken.get());
			})

			.toCompletableFuture()
			.join();
	}

	@Test
	public void shouldEvictAccessTokenAfterExpirarionPeriod() throws Exception {
		caffeineAsyncAccessTokenStorage.findBy(key)
			.thenAccept(accessToken -> assertFalse(accessToken.isPresent()))
			.toCompletableFuture()
			.join();

		AccessToken newAccessToken = AccessToken.bearer("accessToken", Duration.ofSeconds(5));

		caffeineAsyncAccessTokenStorage.add(key, newAccessToken)
			.thenCompose(a -> caffeineAsyncAccessTokenStorage.findBy(key))

			.thenAccept(foundAccessToken -> {
				assertTrue(foundAccessToken.isPresent());
				assertEquals(newAccessToken, foundAccessToken.get());
			})

			.thenRun(() -> Try.run(() -> Thread.sleep(5500)))

			.thenCompose(a -> caffeineAsyncAccessTokenStorage.findBy(key))

			.thenAccept(expiredAccessToken -> assertFalse(expiredAccessToken.isPresent()))

			.toCompletableFuture()
			.join();
	}
}
