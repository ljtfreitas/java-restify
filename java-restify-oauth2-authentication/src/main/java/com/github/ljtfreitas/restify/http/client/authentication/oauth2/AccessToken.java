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
package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.util.Tryable;

public class AccessToken {

	private static final String ACCESS_TOKEN_FIELD = "access_token";
	private static final String TOKEN_TYPE_FIELD = "token_type";
	private static final String EXPIRES_IN_FIELD = "expires_in";
	private static final String SCOPE_FIELD = "scope";
	private static final String REFRESH_TOKEN_FIELD = "refresh_token";

	private final AccessTokenType tokenType;
	private final String token;

	private LocalDateTime expiration = null;
	private Set<String> scopes = Collections.emptySet();
	private String refreshToken = null;

	private Map<String, Object> parameters = new HashMap<>();

	public AccessToken(AccessTokenType tokenType, String token) {
		this.tokenType = tokenType;
		this.token = token;
	}

	public AccessToken(AccessTokenType tokenType, String token, Duration seconds) {
		this(tokenType, token);
		this.expiration = expirationTo(seconds);
	}

	private LocalDateTime expirationTo(Duration seconds) {
		return LocalDateTime.now().plus(seconds);
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

	public LocalDateTime expiration() {
		return expiration;
	}

	public boolean expired() {
		return expiration != null && expiration.isBefore(LocalDateTime.now());
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

	private void parameter(String name, Object value) {
		this.parameters.put(name, value);
	}

	public Map<String, Object> parameters() {
		return parameters;
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

	public static AccessToken create(Parameters parameters) {
		String token = parameters.get(ACCESS_TOKEN_FIELD)
				.orElseThrow(() -> new IllegalStateException("Access token cannot be null!"));

		AccessTokenType tokenType = Tryable.or(() -> AccessTokenType.of(parameters.get(TOKEN_TYPE_FIELD).orElse(null)),
				AccessTokenType.BEARER);

		AccessToken.Builder accessTokenBuilder = AccessToken.Builder.of(tokenType, token);

		parameters.get(EXPIRES_IN_FIELD)
			.ifPresent(expires -> accessTokenBuilder.expiration(Duration.ofSeconds(Long.valueOf(expires))));

		parameters.get(SCOPE_FIELD)
			.ifPresent(scope -> accessTokenBuilder.scope(scope));

		parameters.get(REFRESH_TOKEN_FIELD)
			.ifPresent(refreshToken -> accessTokenBuilder.refreshToken(refreshToken));

		parameters.all().forEach(p -> accessTokenBuilder.parameter(p.name(), p.value()));

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

		public AccessToken.Builder parameter(String name, Object value) {
			accessToken.parameter(name, value);
			return this;
		}

		public AccessToken build() {
			return accessToken;
		}

		public static AccessToken.Builder bearer(String token) {
			return new AccessToken.Builder(AccessTokenType.BEARER, token);
		}

		public static AccessToken.Builder of(AccessTokenType tokenType, String token) {
			return new AccessToken.Builder(tokenType, token);
		}
	}
}
