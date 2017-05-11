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

public class Resource {

	private String id;
	private URI accessTokenUri;
	private ClientCredentials credentials;
	private Collection<String> scopes = Collections.emptyList();

	public String id() {
		return id;
	}

	public ClientCredentials credentials() {
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

		private final Resource resource;

		public Builder() {
			this.resource = new Resource();
		}

		public Builder(Resource resource) {
			this.resource = resource;
		}

		public Builder id(String id) {
			resource.id = id;
			return this;
		}

		public Builder accessTokenUri(String accessTokenUri) {
			resource.accessTokenUri = URI.create(accessTokenUri);
			return this;
		}

		public Builder accessTokenUri(URI accessTokenUri) {
			resource.accessTokenUri = accessTokenUri;
			return this;
		}

		public Builder clientId(String clientId) {
			resource.credentials = ClientCredentials.clientId(clientId);
			return this;
		}

		public Builder credentials(ClientCredentials credentials) {
			resource.credentials = credentials;
			return this;
		}

		public Builder scopes(Collection<String> scopes) {
			resource.scopes = scopes;
			return this;
		}

		public Builder scopes(String... scopes) {
			resource.scopes = Arrays.asList(scopes);
			return this;
		}

		public Resource build() {
			return resource;
		}
	}
}
