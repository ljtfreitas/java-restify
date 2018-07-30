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
package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenStorageKey;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.CaffeineAccessTokenExpirationPolicy;

public class CaffeineAsyncAccessTokenStorage implements AsyncAccessTokenStorage {

	private final AsyncLoadingCache<AccessTokenStorageKey, AccessToken> cache;

	public CaffeineAsyncAccessTokenStorage() {
		this.cache = Caffeine.newBuilder()
				.expireAfter(new CaffeineAccessTokenExpirationPolicy())
				.buildAsync(key -> null);
	}

	public CaffeineAsyncAccessTokenStorage(CaffeineSpec spec) {
		this(Caffeine.from(spec).buildAsync(key -> null));
	}

	public CaffeineAsyncAccessTokenStorage(AsyncLoadingCache<AccessTokenStorageKey, AccessToken> cache) {
		this.cache = cache;
	}

	@Override
	public CompletionStage<Optional<AccessToken>> findBy(AccessTokenStorageKey key) {
		return cache.get(key).thenApply(accessToken -> Optional.ofNullable(accessToken));
	}

	@Override
	public CompletionStage<Void> add(AccessTokenStorageKey key, AccessToken accessToken) {
		CompletableFuture<AccessToken> accessTokenAsFuture = CompletableFuture.completedFuture(accessToken);
		return accessTokenAsFuture.thenAccept(a -> cache.put(key, accessTokenAsFuture));
	}
}
