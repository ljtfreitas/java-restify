package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

public class OAuth2AuthorizationConfiguration extends OAuth2Configuration {

	private String authorizationCode;
	private URI authorizationUri;
	private URI redirectUri;

	public Optional<String> authorizationCode() {
		return Optional.ofNullable(authorizationCode);
	}

	public URI authorizationUri() {
		return authorizationUri;
	}

	public URI redirectUri() {
		return redirectUri;
	}

	public static class Builder {

		private final OAuth2AuthorizationConfiguration configuration = new OAuth2AuthorizationConfiguration();
		private final OAuth2Configuration.Builder authenticationConfigurationBuilder = new OAuth2Configuration.Builder(configuration);

		public Builder accessTokenUri(String accessTokenUri) {
			authenticationConfigurationBuilder.accessTokenUri(accessTokenUri);
			return this;
		}

		public Builder accessTokenUri(URI accessTokenUri) {
			authenticationConfigurationBuilder.accessTokenUri(accessTokenUri);
			return this;
		}

		public Builder clientId(String clientId) {
			authenticationConfigurationBuilder.clientId(clientId);
			return this;
		}

		public Builder credentials(OAuth2ClientCredentials credentials) {
			authenticationConfigurationBuilder.credentials(credentials);
			return this;
		}

		public Builder scopes(Collection<String> scopes) {
			authenticationConfigurationBuilder.scopes(scopes);
			return this;
		}

		public Builder scopes(String... scopes) {
			authenticationConfigurationBuilder.scopes(scopes);
			return this;
		}

		public Builder authorizationCode(String authorizationCode) {
			configuration.authorizationCode = authorizationCode;
			return this;
		}

		public Builder authorizationUri(URI authorizationUri) {
			configuration.authorizationUri = authorizationUri;
			return this;
		}

		public Builder authorizationUri(String authorizationUri) {
			configuration.authorizationUri = URI.create(authorizationUri);
			return this;
		}

		public Builder redirectUri(URI redirectUri) {
			configuration.redirectUri = redirectUri;
			return this;
		}

		public Builder redirectUri(String redirectUri) {
			configuration.redirectUri = URI.create(redirectUri);
			return this;
		}

		public OAuth2AuthorizationConfiguration build() {
			return configuration;
		}
	}
}
