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

import java.net.URI;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.message.Cookie;
import com.github.ljtfreitas.restify.http.client.message.Cookies;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedParametersMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.XmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.spi.Provider;

public class OAuth2AuthenticationBuilder {

	private OAuth2AuthenticationGrantTypeBuilder grantTypeBuilder = new OAuth2AuthenticationGrantTypeBuilder(this);
	private OAuth2AuthorizationServerBuilder authorizationServerBuilder = new OAuth2AuthorizationServerBuilder(this);

	private AccessTokenProvider accessTokenProvider;
	private AccessTokenRepository accessTokenRepository = null;
	private AccessTokenStorage accessTokenStorage = null;

	public OAuth2AuthenticationGrantTypeBuilder grantType() {
		return grantTypeBuilder;
	}

	public OAuth2AuthenticationBuilder repository(AccessTokenRepository accessTokenRepository) {
		this.accessTokenRepository = accessTokenRepository;
		return this;
	}

	public OAuth2AuthenticationBuilder storage(AccessTokenStorage accessTokenStorage) {
		this.accessTokenStorage = accessTokenStorage;
		return this;
	}

	public OAuth2AuthenticationBuilder provider(AccessTokenProvider accessTokenProvider) {
		this.accessTokenProvider = accessTokenProvider;
		return this;
	}

	public OAuth2AuthorizationServerBuilder authorizationServer() {
		return authorizationServerBuilder;
	}

	public OAuth2Authentication build() {
		return new OAuth2Authentication(grantTypeProperties(), accessTokenRepository(), null);
	}

	private AccessTokenRepository accessTokenRepository() {
		return Optional.ofNullable(accessTokenRepository)
				.orElseGet(() -> new DefaultAccessTokenRepository(accessTokenProvider(), accessTokenStorage()));
	}

	private GrantProperties grantTypeProperties() {
		return nonNull(grantTypeBuilder.properties, "Grant parameters must be configured.").build();
	}

	private AccessTokenProvider accessTokenProvider() {
		return Optional.ofNullable(accessTokenProvider)
				.orElseGet(() -> nonNull(grantTypeBuilder.properties, "Grant parameters must be configured.")
						.createProviderWith(authorizationServerBuilder.build()));
	}

	private AccessTokenStorage accessTokenStorage() {
		return Optional.ofNullable(accessTokenStorage)
			.orElseGet(() -> new Provider().single(AccessTokenStorage.class)
				.orElseGet(AccessTokenMemoryStorage::new));
	}

	public class OAuth2AuthorizationServerBuilder {

		private final OAuth2AuthenticationBuilder context;

		private AuthorizationServer authorizationServer;
		private EndpointRequestExecutor endpointRequestExecutor;
		private HttpClientRequestFactory httpClientRequestFactory;
		private ClientAuthenticationMethod authenticationMethod = ClientAuthenticationMethod.HEADER;

		private OAuth2AuthorizationServerHttpMessageConvertersBuilder httpMessageConvertersBuilder;

		private OAuth2AuthorizationServerBuilder(OAuth2AuthenticationBuilder context) {
			this.context = context;
		}

		public OAuth2AuthorizationServerBuilder executor(EndpointRequestExecutor endpointRequestExecutor) {
			this.endpointRequestExecutor = endpointRequestExecutor;
			return this;
		}

		public OAuth2AuthorizationServerHttpMessageConvertersBuilder converters() {
			return (httpMessageConvertersBuilder = new OAuth2AuthorizationServerHttpMessageConvertersBuilder(this));
		}

		public OAuth2AuthorizationServerBuilder converters(HttpMessageConverter... converters) {
			this.httpMessageConvertersBuilder = new OAuth2AuthorizationServerHttpMessageConvertersBuilder(this,
					Arrays.asList(converters));
			return this;
		}

		public OAuth2AuthorizationServerBuilder client(HttpClientRequestFactory httpClientRequestFactory) {
			this.httpClientRequestFactory = httpClientRequestFactory;
			return this;
		}

		public OAuth2AuthorizationServerBuilder method(ClientAuthenticationMethod authenticationMethod) {
			this.authenticationMethod = authenticationMethod;
			return this;
		}

		public OAuth2AuthenticationBuilder using(AuthorizationServer authorizationServer) {
			this.authorizationServer = authorizationServer;
			return context;
		}

		public OAuth2AuthenticationBuilder and() {
			return context;
		}

		private AuthorizationServer build() {
			return Optional.ofNullable(authorizationServer)
					.orElseGet(() -> withEndpointRequestExecutor()
							.orElseGet(() -> withHttpMessageConverters().orElseGet(() -> withHttpClientRequestFactory()
									.orElseGet(() -> new DefaultAuthorizationServer(authenticationMethod)))));
		}

		private Optional<AuthorizationServer> withEndpointRequestExecutor() {
			return Optional.ofNullable(endpointRequestExecutor)
					.map(e -> new DefaultAuthorizationServer(e, authenticationMethod));
		}

		private Optional<AuthorizationServer> withHttpMessageConverters() {
			return Optional.ofNullable(httpMessageConvertersBuilder).map(b -> b.build())
					.map(c -> new DefaultAuthorizationServer(c, authenticationMethod));
		}

		private Optional<AuthorizationServer> withHttpClientRequestFactory() {
			return Optional.ofNullable(httpClientRequestFactory)
					.map(f -> new DefaultAuthorizationServer(f, authenticationMethod));
		}

		public class OAuth2AuthorizationServerHttpMessageConvertersBuilder {

			private final OAuth2AuthorizationServerBuilder delegate;
			private final Collection<HttpMessageConverter> converters = new ArrayList<>(Arrays.asList(
					new TextPlainMessageConverter(), new FormURLEncodedParametersMessageConverter()));

			private final Provider provider = new Provider();

			private OAuth2AuthorizationServerHttpMessageConvertersBuilder(OAuth2AuthorizationServerBuilder context) {
				this.delegate = context;
			}

			private OAuth2AuthorizationServerHttpMessageConvertersBuilder(OAuth2AuthorizationServerBuilder context,
					Collection<HttpMessageConverter> converters) {
				this.delegate = context;
				this.converters.addAll(converters);
			}

			public OAuth2AuthorizationServerHttpMessageConvertersBuilder json() {
				provider.single(JsonMessageConverter.class).ifPresent(converters::add);
				return this;
			}

			public OAuth2AuthorizationServerHttpMessageConvertersBuilder xml() {
				provider.single(XmlMessageConverter.class).ifPresent(converters::add);
				return this;
			}

			public OAuth2AuthenticationBuilder and() {
				return delegate.context;
			}

			private HttpMessageConverters build() {
				return new HttpMessageConverters(converters);
			}
		}
	}

	public class OAuth2AuthenticationGrantTypeBuilder {

		private final OAuth2AuthenticationBuilder context;

		private OAuth2AuthenticationGrantPropertiesBuilder properties = null;

		private OAuth2AuthenticationGrantTypeBuilder(OAuth2AuthenticationBuilder context) {
			this.context = context;
		}

		public OAuth2ClientCredentialsGrantBuilder clientCredentials() {
			properties = new OAuth2ClientCredentialsGrantBuilder(context);
			return (OAuth2ClientCredentialsGrantBuilder) properties;
		}

		public OAuth2ResourceOwnerGrantBuilder resourceOwner() {
			properties = new OAuth2ResourceOwnerGrantBuilder(context);
			return (OAuth2ResourceOwnerGrantBuilder) properties;
		}

		public OAuth2ImplicitGrantBuilder implicit() {
			properties = new OAuth2ImplicitGrantBuilder(context);
			return (OAuth2ImplicitGrantBuilder) properties;
		}

		public OAuth2AuthorizationCodeGrantBuilder authorizationCode() {
			properties = new OAuth2AuthorizationCodeGrantBuilder(context);
			return (OAuth2AuthorizationCodeGrantBuilder) properties;
		}
	}

	private abstract class OAuth2AccessTokenProviderFactory {
		abstract AccessTokenProvider createProviderWith(AuthorizationServer authorizationServer);
	}

	private abstract class OAuth2AuthenticationGrantPropertiesBuilder extends OAuth2AccessTokenProviderFactory {
		abstract GrantProperties build();
	}

	public class OAuth2ClientCredentialsGrantBuilder extends OAuth2AuthenticationGrantPropertiesBuilder {

		private final OAuth2AuthenticationBuilder context;
		private final ClientCredentialsGrantProperties.Builder delegate = GrantProperties.Builder.clientCredentials();

		private OAuth2ClientCredentialsGrantBuilder(OAuth2AuthenticationBuilder context) {
			this.context = context;
		}

		public OAuth2ClientCredentialsGrantBuilder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2ClientCredentialsGrantBuilder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2ClientCredentialsGrantBuilder accessTokenUri(URL accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2ClientCredentialsGrantBuilder credentials(String clientId, String clientSecret) {
			delegate.credentials(clientId, clientSecret);
			return this;
		}

		public OAuth2ClientCredentialsGrantBuilder credentials(ClientCredentials credentials) {
			delegate.credentials(credentials);
			return this;
		}

		public OAuth2ClientCredentialsGrantBuilder scopes(Collection<String> scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2ClientCredentialsGrantBuilder scopes(String... scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2ClientCredentialsGrantBuilder user(Principal user) {
			delegate.user(user);
			return this;
		}

		public OAuth2AuthenticationBuilder and() {
			return context;
		}

		@Override
		protected GrantProperties build() {
			return delegate.build();
		}

		@Override
		protected AccessTokenProvider createProviderWith(AuthorizationServer authorizationServer) {
			return new ClientCredentialsAccessTokenProvider(authorizationServer);
		}
	}

	public class OAuth2ResourceOwnerGrantBuilder extends OAuth2AuthenticationGrantPropertiesBuilder {

		private final OAuth2AuthenticationBuilder context;
		private final ResourceOwnerGrantProperties.Builder delegate = GrantProperties.Builder.resourceOwner();

		private OAuth2ResourceOwnerGrantBuilder(OAuth2AuthenticationBuilder context) {
			this.context = context;
		}

		public OAuth2ResourceOwnerGrantBuilder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2ResourceOwnerGrantBuilder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2ResourceOwnerGrantBuilder accessTokenUri(URL accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2ResourceOwnerGrantBuilder clientId(String clientId) {
			delegate.clientId(clientId);
			return this;
		}

		public OAuth2ResourceOwnerGrantBuilder credentials(String clientId, String clientSecret) {
			delegate.credentials(clientId, clientSecret);
			return this;
		}

		public OAuth2ResourceOwnerGrantBuilder credentials(ClientCredentials credentials) {
			delegate.credentials(credentials);
			return this;
		}

		public OAuth2ResourceOwnerGrantBuilder scopes(Collection<String> scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2ResourceOwnerGrantBuilder scopes(String... scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2ResourceOwnerGrantBuilder user(Principal user) {
			delegate.user(user);
			return this;
		}

		public OAuth2ResourceOwnerGrantBuilder resourceOwner(ResourceOwner resourceOwner) {
			delegate.resourceOwner(resourceOwner);
			return this;
		}

		public OAuth2ResourceOwnerGrantBuilder resourceOwner(String username, String password) {
			delegate.resourceOwner(username, password);
			return this;
		}

		public OAuth2AuthenticationBuilder and() {
			return context;
		}

		@Override
		protected GrantProperties build() {
			return delegate.build();
		}

		@Override
		protected AccessTokenProvider createProviderWith(AuthorizationServer authorizationServer) {
			return new ResourceOwnerPasswordAccessTokenProvider(authorizationServer);
		}
	}

	public class OAuth2ImplicitGrantBuilder extends OAuth2AuthenticationGrantPropertiesBuilder {

		private final OAuth2AuthenticationBuilder context;
		private final ImplicitGrantProperties.Builder delegate = GrantProperties.Builder.implicit();

		private OAuth2ImplicitGrantBuilder(OAuth2AuthenticationBuilder context) {
			this.context = context;
		}

		public OAuth2ImplicitGrantBuilder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2ImplicitGrantBuilder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2ImplicitGrantBuilder accessTokenUri(URL accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2ImplicitGrantBuilder clientId(String clientId) {
			delegate.clientId(clientId);
			return this;
		}

		public OAuth2ImplicitGrantBuilder credentials(String clientId, String clientSecret) {
			delegate.credentials(clientId, clientSecret);
			return this;
		}

		public OAuth2ImplicitGrantBuilder credentials(ClientCredentials credentials) {
			delegate.credentials(credentials);
			return this;
		}

		public OAuth2ImplicitGrantBuilder scopes(Collection<String> scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2ImplicitGrantBuilder scopes(String... scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2ImplicitGrantBuilder user(Principal user) {
			delegate.user(user);
			return this;
		}

		public OAuth2ImplicitGrantBuilder authorizationCode(String authorizationCode) {
			delegate.authorizationCode(authorizationCode);
			return this;
		}

		public OAuth2ImplicitGrantBuilder authorizationUri(URI authorizationUri) {
			delegate.authorizationUri(authorizationUri);
			return this;
		}

		public OAuth2ImplicitGrantBuilder authorizationUri(String authorizationUri) {
			delegate.authorizationUri(authorizationUri);
			return this;
		}

		public OAuth2ImplicitGrantBuilder redirectUri(URI redirectUri) {
			delegate.redirectUri(redirectUri);
			return this;
		}

		public OAuth2ImplicitGrantBuilder redirectUri(String redirectUri) {
			delegate.redirectUri(redirectUri);
			return this;
		}

		public OAuth2ImplicitGrantBuilder responseType(String responseType) {
			delegate.responseType(responseType);
			return this;
		}

		public OAuth2ImplicitGrantBuilder cookie(String cookie) {
			delegate.cookie(cookie);
			return this;
		}

		public OAuth2ImplicitGrantBuilder cookie(Cookie cookie) {
			delegate.cookie(cookie);
			return this;
		}

		public OAuth2ImplicitGrantBuilder cookie(String name, String value) {
			delegate.cookie(name, value);
			return this;
		}

		public OAuth2ImplicitGrantBuilder cookies(Cookies cookies) {
			delegate.cookies(cookies);
			return this;
		}

		public OAuth2ImplicitGrantBuilder state(String state) {
			delegate.state(state);
			return this;
		}

		public OAuth2ImplicitGrantBuilder header(String name, String value) {
			delegate.header(name, value);
			return this;
		}

		public OAuth2ImplicitGrantBuilder header(Header header) {
			delegate.header(header);
			return this;
		}

		public OAuth2ImplicitGrantBuilder headers(Header... headers) {
			delegate.headers(headers);
			return this;
		}

		public OAuth2ImplicitGrantBuilder headers(Headers headers) {
			delegate.headers(headers);
			return this;
		}

		public OAuth2AuthenticationBuilder and() {
			return context;
		}

		@Override
		protected GrantProperties build() {
			return delegate.build();
		}

		@Override
		protected AccessTokenProvider createProviderWith(AuthorizationServer authorizationServer) {
			return new ImplicitAccessTokenProvider(authorizationServer);
		}
	}

	public class OAuth2AuthorizationCodeGrantBuilder extends OAuth2AuthenticationGrantPropertiesBuilder {

		private final OAuth2AuthenticationBuilder context;
		private final AuthorizationCodeGrantProperties.Builder delegate = GrantProperties.Builder.authorizationCode();

		private AuthorizationCodeProvider authorizationCodeProvider;

		private OAuth2AuthorizationCodeGrantBuilder(OAuth2AuthenticationBuilder context) {
			this.context = context;
		}

		public OAuth2AuthorizationCodeGrantBuilder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder accessTokenUri(URL accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder clientId(String clientId) {
			delegate.clientId(clientId);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder credentials(ClientCredentials credentials) {
			delegate.credentials(credentials);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder credentials(String clientId, String clientSecret) {
			delegate.credentials(clientId, clientSecret);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder scopes(Collection<String> scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder scopes(String... scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder user(Principal user) {
			delegate.user(user);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder authorizationCode(String authorizationCode) {
			delegate.authorizationCode(authorizationCode);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder authorizationUri(URI authorizationUri) {
			delegate.authorizationUri(authorizationUri);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder authorizationUri(String authorizationUri) {
			delegate.authorizationUri(authorizationUri);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder redirectUri(URI redirectUri) {
			delegate.redirectUri(redirectUri);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder redirectUri(String redirectUri) {
			delegate.redirectUri(redirectUri);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder responseType(String responseType) {
			delegate.responseType(responseType);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder cookie(String cookie) {
			delegate.cookie(cookie);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder cookie(Cookie cookie) {
			delegate.cookie(cookie);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder cookie(String name, String value) {
			delegate.cookie(name, value);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder cookies(Cookies cookies) {
			delegate.cookies(cookies);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder state(String state) {
			delegate.state(state);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder header(String name, String value) {
			delegate.header(name, value);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder header(Header header) {
			delegate.header(header);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder headers(Header... headers) {
			delegate.headers(headers);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder headers(Headers headers) {
			delegate.headers(headers);
			return this;
		}

		public OAuth2AuthorizationCodeGrantBuilder authorizationCodeProvider(
				AuthorizationCodeProvider authorizationCodeProvider) {
			this.authorizationCodeProvider = authorizationCodeProvider;
			return this;
		}

		public OAuth2AuthenticationBuilder and() {
			return context;
		}

		@Override
		protected GrantProperties build() {
			return delegate.build();
		}

		@Override
		protected AccessTokenProvider createProviderWith(AuthorizationServer authorizationServer) {
			AuthorizationCodeProvider authorizationCodeProvider = Optional.ofNullable(this.authorizationCodeProvider)
					.orElseGet(() -> new DefaultAuthorizationCodeProvider(authorizationServer));

			return new AuthorizationCodeAccessTokenProvider(authorizationCodeProvider, authorizationServer);
		}
	}
}
