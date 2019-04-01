/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.spi.CachingProvider;

public class JCacheAccessTokenStorage implements AccessTokenStorage {

	private static final String CACHE_NAME_PREFIX = "access-token.";

	private final CacheManager cacheManager;

	public JCacheAccessTokenStorage() {
		this(Caching.getCachingProvider());
	}

	public JCacheAccessTokenStorage(CachingProvider cachingProvider) {
		this(cachingProvider.getCacheManager());
	}

	public JCacheAccessTokenStorage(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public Optional<AccessToken> findBy(AccessTokenStorageKey key) {
		return cacheTo(key).map(cache -> cache.get(key));
	}

	@Override
	public void add(AccessTokenStorageKey key, AccessToken accessToken) {
		cacheTo(key).orElseGet(() -> newCacheTo(key, accessToken.expiration())).put(key, accessToken);
	}

	private Optional<Cache<AccessTokenStorageKey, AccessToken>> cacheTo(AccessTokenStorageKey key) {
		return Optional.ofNullable(cacheManager.getCache(CACHE_NAME_PREFIX + key));
	}

	private Cache<AccessTokenStorageKey, AccessToken> newCacheTo(AccessTokenStorageKey key, Optional<Instant> expiration) {
		MutableConfiguration<AccessTokenStorageKey, AccessToken> configuration = new MutableConfiguration<>();

		Factory<ExpiryPolicy> policy = CreatedExpiryPolicy.factoryOf(durationUntil(expiration));

		configuration
			.setTypes(AccessTokenStorageKey.class, AccessToken.class)
			.setStoreByValue(true)
			.setExpiryPolicyFactory(policy);

		return cacheManager.createCache(CACHE_NAME_PREFIX + key, configuration);
	}

	private Duration durationUntil(Optional<Instant> expiration) {
		return expiration
			.map(end -> java.time.Duration.between(Instant.now(), end))
				.map(d -> new Duration(TimeUnit.MILLISECONDS, d.toMillis()))
					.orElseGet(() -> Duration.ETERNAL);
	}
}
