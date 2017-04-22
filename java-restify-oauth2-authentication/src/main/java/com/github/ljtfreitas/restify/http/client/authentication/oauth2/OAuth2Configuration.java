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
