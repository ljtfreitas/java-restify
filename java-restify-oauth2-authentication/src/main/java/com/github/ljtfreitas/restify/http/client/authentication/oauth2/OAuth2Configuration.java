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

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class OAuth2Configuration {

	private URI accessTokenUri;
	private OAuth2ClientCredentials credentials;
	private Collection<String> scopes = Collections.emptyList();

	public OAuth2ClientCredentials credentials() {
		return credentials;
	}

	public Collection<String> scopes() {
		return scopes;
	}

	public String scope() {
		return scopes.stream().collect(Collectors.joining(" "));
	}

	public URI accessTokenUri() {
		return accessTokenUri;
	}

	public static class Builder {

		private final OAuth2Configuration configuration;

		public Builder() {
			this.configuration = new OAuth2Configuration();
		}

		public Builder(OAuth2Configuration configuration) {
			this.configuration = configuration;
		}

		public Builder accessTokenUri(String accessTokenUri) {
			configuration.accessTokenUri = URI.create(accessTokenUri);
			return this;
		}

		public Builder accessTokenUri(URI accessTokenUri) {
			configuration.accessTokenUri = accessTokenUri;
			return this;
		}

		public Builder clientId(String clientId) {
			configuration.credentials = OAuth2ClientCredentials.clientId(clientId);
			return this;
		}

		public Builder credentials(OAuth2ClientCredentials credentials) {
			configuration.credentials = credentials;
			return this;
		}

		public Builder scopes(Collection<String> scopes) {
			configuration.scopes = scopes;
			return this;
		}

		public Builder scopes(String... scopes) {
			configuration.scopes = Arrays.asList(scopes);
			return this;
		}

		public OAuth2Configuration build() {
			return configuration;
		}
	}
}
