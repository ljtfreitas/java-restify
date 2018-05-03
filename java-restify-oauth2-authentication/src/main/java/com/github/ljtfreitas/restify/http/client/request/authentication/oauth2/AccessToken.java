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

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.util.Tryable;

public class AccessToken implements Serializable {

	private static final long serialVersionUID = 1L;

	private final AccessTokenType tokenType;
	private final String token;

	private Instant expiration = null;
	private Set<String> scopes = Collections.emptySet();
	private String refreshToken = null;

	public AccessToken(AccessTokenType tokenType, String token) {
		this.tokenType = tokenType;
		this.token = token;
	}

	public AccessToken(AccessTokenType tokenType, String token, Duration seconds) {
		this(tokenType, token);
		this.expiration = expirationTo(seconds);
	}

	private Instant expirationTo(Duration seconds) {
		return Instant.now().plus(seconds);
	}

	public AccessTokenType type() {
		return tokenType;
	}

	public String token() {
		return token;
	}

	private AccessToken expiration(long seconds) {
		this.expiration = expirationTo(Duration.ofSeconds(seconds));
		return this;
	}

	private AccessToken expiration(Duration seconds) {
		this.expiration = expirationTo(seconds);
		return this;
	}

	public Optional<Instant> expiration() {
		return Optional.ofNullable(expiration);
	}

	public boolean expired() {
		return expiration != null && expiration.isBefore(Instant.now());
	}

	private AccessToken scope(String scope) {
		this.scopes = Arrays.stream(scope.split(" ")).collect(Collectors.toSet());
		return this;
	}

	private AccessToken scopes(String... scopes) {
		this.scopes = Arrays.stream(scopes).collect(Collectors.toSet());
		return this;
	}

	private AccessToken scopes(Set<String> scopes) {
		this.scopes = Collections.unmodifiableSet(scopes);
		return this;
	}

	public Set<String> scopes() {
		return scopes;
	}

	public String scope() {
		return scopes.stream().collect(Collectors.joining(" "));
	}

	private void refreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Optional<String> refreshToken() {
		return Optional.ofNullable(refreshToken);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tokenType, token);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AccessToken) {
			AccessToken that = (AccessToken) obj;

			return this.tokenType.equals(that.tokenType)
				&& this.token.equals(that.token);

		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return tokenType + " " + token;
	}

	public static AccessToken bearer(String token) {
		return new AccessToken(AccessTokenType.BEARER, token);
	}

	public static AccessToken bearer(String token, Duration duration) {
		return new AccessToken(AccessTokenType.BEARER, token, duration);
	}

	public static AccessToken of(AccessTokenResponse source) {
		AccessTokenType tokenType = Optional.ofNullable(source.tokenType())
				.map(t -> Tryable.or(() -> AccessTokenType.of(t), AccessTokenType.BEARER))
					.orElse(AccessTokenType.BEARER);

		AccessToken.Builder accessTokenBuilder = AccessToken.Builder.of(tokenType, source.accessToken());

		Optional.ofNullable(source.expires())
			.ifPresent(expires -> accessTokenBuilder.expiration(Duration.ofSeconds(Long.valueOf(expires))));

		Optional.ofNullable(source.scope())
			.ifPresent(scope -> accessTokenBuilder.scope(scope));

		Optional.ofNullable(source.refreshToken())
			.ifPresent(refreshToken -> accessTokenBuilder.refreshToken(refreshToken));

		return accessTokenBuilder.build();
	}

	public static class Builder {

		private final AccessToken accessToken;

		public Builder(AccessTokenType tokenType, String token) {
			this.accessToken = new AccessToken(tokenType, token);
		}

		public AccessToken.Builder expiration(long seconds) {
			accessToken.expiration(seconds);
			return this;
		}

		public AccessToken.Builder expiration(Duration seconds) {
			accessToken.expiration(seconds);
			return this;
		}

		public AccessToken.Builder scope(String scope) {
			accessToken.scope(scope);
			return this;
		}

		public AccessToken.Builder scopes(String... scopes) {
			accessToken.scopes(scopes);
			return this;
		}

		public AccessToken.Builder scopes(Set<String> scopes) {
			accessToken.scopes(scopes);
			return this;
		}

		public AccessToken.Builder refreshToken(String refreshToken) {
			accessToken.refreshToken(refreshToken);
			return this;
		}

		public AccessToken build() {
			return accessToken;
		}

		public static AccessToken.Builder bearer(String token) {
			return new AccessToken.Builder(AccessTokenType.BEARER, token);
		}

		public static AccessToken.Builder of(AccessTokenType tokenType, String token) {
			nonNull(tokenType, "Token type can't be null.");
			nonNull(token, "Access token can't be null.");
			return new AccessToken.Builder(tokenType, token);
		}
	}
}
