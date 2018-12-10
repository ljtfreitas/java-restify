package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.security.Principal;
import java.time.Duration;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenStorageKey;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;
import com.github.ljtfreitas.restify.util.Tryable;
import com.github.ljtfreitas.restify.util.async.DisposableExecutors;

public class JCacheAsyncAccessTokenStorageTest {

	private JCacheAsyncAccessTokenStorage jCacheAsyncAccessTokenStorage;

	private AccessTokenStorageKey key;

	private CacheManager cacheManager;

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

		cacheManager = Caching.getCachingProvider().getCacheManager();

		jCacheAsyncAccessTokenStorage = new JCacheAsyncAccessTokenStorage(DisposableExecutors.newSingleThreadExecutor(), cacheManager);
	}

	@After
	public void after() {
		cacheManager.getCacheNames().forEach(cache -> cacheManager.destroyCache(cache));
	}

	@Test
	public void shouldSaveAccessTokenOnStore() {
		jCacheAsyncAccessTokenStorage.findBy(key)

			.thenAccept(accessToken -> assertFalse(accessToken.isPresent()))

			.toCompletableFuture()
			.join();

		AccessToken accessToken = AccessToken.bearer("accessToken");

		jCacheAsyncAccessTokenStorage.add(key, accessToken)

			.thenCompose(a -> jCacheAsyncAccessTokenStorage.findBy(key))

			.thenAccept(foundAccessToken -> {
				assertTrue(foundAccessToken.isPresent());
				assertEquals(accessToken, foundAccessToken.get());
			})

			.toCompletableFuture()
			.join();
	}

	@Test
	public void shouldEvictAccessTokenAfterExpirationPeriod() throws Exception {
		jCacheAsyncAccessTokenStorage.findBy(key)
			.thenAccept(accessToken -> assertFalse(accessToken.isPresent()))
			.toCompletableFuture()
			.join();

		AccessToken newAccessToken = AccessToken.bearer("accessToken", Duration.ofSeconds(5));

		jCacheAsyncAccessTokenStorage.add(key, newAccessToken)
			.thenCompose(a -> jCacheAsyncAccessTokenStorage.findBy(key))

			.thenAccept(foundAccessToken -> {
				assertTrue(foundAccessToken.isPresent());
				assertEquals(newAccessToken, foundAccessToken.get());
			})

			.thenRun(() -> Tryable.run(() -> Thread.sleep(5500)))

			.thenCompose(a -> jCacheAsyncAccessTokenStorage.findBy(key))

			.thenAccept(expiredAccessToken -> assertFalse(expiredAccessToken.isPresent()))

			.toCompletableFuture()
			.join();
	}
}
