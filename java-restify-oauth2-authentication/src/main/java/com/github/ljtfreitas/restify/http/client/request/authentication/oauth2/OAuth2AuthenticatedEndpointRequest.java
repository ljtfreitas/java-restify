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

import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.util.Tryable;

public class OAuth2AuthenticatedEndpointRequest {

	private final EndpointRequest source;
	private final GrantProperties properties;
	private final Principal user;
	private final String scope;

	public OAuth2AuthenticatedEndpointRequest(EndpointRequest source, GrantProperties properties) {
		this(source, properties, null);
	}

	public OAuth2AuthenticatedEndpointRequest(EndpointRequest source, GrantProperties properties, Principal user) {
		this.source = source;
		this.properties = properties;
		this.user = Optional.ofNullable(user).orElseGet(() -> properties.user().orElse(null));
		this.scope = scopes(properties, source);
	}

	private String scopes(GrantProperties properties, EndpointRequest source) {
		return Stream.concat(properties.scopes().stream(),
				source.metadata().all(Scope.class).stream().flatMap(scope -> Arrays.stream(scope.value())))
					.collect(Collectors.joining(" "));
	}

	public EndpointRequest source() {
		return source;
	}

	public URI endpoint() {
		return source.endpoint();
	}

	public GrantProperties properties() {
		return properties;
	}

	public <T extends GrantProperties> T properties(Class<T> type) {
		return Tryable.of(() -> type.cast(properties),
				() -> new ClassCastException("Your GrantProperties must be a instance of [" + type + "]."));
	}

	public Optional<Principal> user() {
		return Optional.ofNullable(user);
	}

	public String clientId() {
		return properties.credentials().clientId();
	}

	public String scope() {
		return scope;
	}

	public URI accessTokenUri() {
		return properties.accessTokenUri();
	}

	public ClientCredentials credentials() {
		return properties.credentials();
	}
}
