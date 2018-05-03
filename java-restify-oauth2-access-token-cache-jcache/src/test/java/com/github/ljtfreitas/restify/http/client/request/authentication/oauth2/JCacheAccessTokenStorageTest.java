package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.security.Principal;
import java.time.Duration;
import java.util.Optional;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;

public class JCacheAccessTokenStorageTest {

	private JCacheAccessTokenStorage jCacheAccessTokenStorage;

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

		jCacheAccessTokenStorage = new JCacheAccessTokenStorage(cacheManager);
	}

	@After
	public void after() {
		cacheManager.getCacheNames().forEach(cache -> cacheManager.destroyCache(cache));
	}

	@Test
	public void shouldSaveAccessTokenOnStore() {
		assertFalse(jCacheAccessTokenStorage.findBy(key).isPresent());

		AccessToken accessToken = AccessToken.bearer("accessToken");

		jCacheAccessTokenStorage.add(key, accessToken);

		Optional<AccessToken> foundAccessToken = jCacheAccessTokenStorage.findBy(key);

		assertTrue(foundAccessToken.isPresent());
		assertEquals(accessToken, foundAccessToken.get());
	}

	@Test
	public void shouldEvictAccessTokenAfterExpirarionPeriod() throws Exception {
		assertFalse(jCacheAccessTokenStorage.findBy(key).isPresent());

		AccessToken accessToken = AccessToken.bearer("accessToken", Duration.ofSeconds(5));

		jCacheAccessTokenStorage.add(key, accessToken);

		assertTrue(jCacheAccessTokenStorage.findBy(key).isPresent());

		Thread.sleep(5500);

		assertFalse(jCacheAccessTokenStorage.findBy(key).isPresent());
	}
}
