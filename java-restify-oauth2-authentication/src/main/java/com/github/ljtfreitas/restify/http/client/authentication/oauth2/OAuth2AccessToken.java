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
import java.util.Set;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.util.Tryable;

public class OAuth2AccessToken {

	private static final String ACCESS_TOKEN_FIELD = "access_token";
	private static final String TOKEN_TYPE_FIELD = "token_type";
	private static final String EXPIRES_IN_FIELD = "expires_in";
	private static final String SCOPE_FIELD = "scope";

	private final OAuth2AccessTokenType tokenType;
	private final String token;

	private LocalDateTime expiration = null;
	private Set<String> scopes = Collections.emptySet();

	public OAuth2AccessToken(OAuth2AccessTokenType tokenType, String token) {
		this.tokenType = tokenType;
		this.token = token;
	}

	public OAuth2AccessToken(OAuth2AccessTokenType tokenType, String token, Duration seconds) {
		this(tokenType, token);
		this.expiration = expirationTo(seconds);
	}

	private LocalDateTime expirationTo(Duration seconds) {
		return LocalDateTime.now().plus(seconds);
	}

	public OAuth2AccessTokenType type() {
		return tokenType;
	}

	public String token() {
		return token;
	}

	@Override
	public String toString() {
		return tokenType + " " + token;
	}

	public OAuth2AccessToken expiration(long seconds) {
		this.expiration = expirationTo(Duration.ofSeconds(seconds));
		return this;
	}

	public OAuth2AccessToken expiration(Duration seconds) {
		this.expiration = expirationTo(seconds);
		return this;
	}

	public LocalDateTime expiration() {
		return expiration;
	}

	public boolean expired() {
		return expiration != null && expiration.isBefore(LocalDateTime.now());
	}

	public OAuth2AccessToken scope(String scope) {
		this.scopes = Arrays.stream(scope.split(" ")).collect(Collectors.toSet());
		return this;
	}

	public OAuth2AccessToken scopes(String... scopes) {
		this.scopes = Arrays.stream(scopes).collect(Collectors.toSet());
		return this;
	}

	public OAuth2AccessToken scopes(Set<String> scopes) {
		this.scopes = Collections.unmodifiableSet(scopes);
		return this;
	}

	public Set<String> scopes() {
		return scopes;
	}

	public String scope() {
		return scopes.stream().collect(Collectors.joining(" "));
	}

	public static OAuth2AccessToken bearer(String token) {
		return new OAuth2AccessToken(OAuth2AccessTokenType.BEARER, token);
	}

	public static OAuth2AccessToken bearer(String token, Duration duration) {
		return new OAuth2AccessToken(OAuth2AccessTokenType.BEARER, token, duration);
	}

	public static OAuth2AccessToken create(Parameters parameters) {
		String token = parameters.get(ACCESS_TOKEN_FIELD).orElseThrow(() -> new IllegalStateException("Access token cannot be null!"));

		OAuth2AccessTokenType tokenType = Tryable.or(() -> OAuth2AccessTokenType.of(parameters.get(TOKEN_TYPE_FIELD).orElse(null)),
				OAuth2AccessTokenType.BEARER);

		OAuth2AccessToken accessToken = new OAuth2AccessToken(tokenType, token);

		parameters.get(EXPIRES_IN_FIELD)
			.ifPresent(expires -> accessToken.expiration(Duration.ofSeconds(Long.valueOf(expires))));

		parameters.get(SCOPE_FIELD)
			.ifPresent(scope -> accessToken.scope(scope.toString()));

		return accessToken;
	}
}
