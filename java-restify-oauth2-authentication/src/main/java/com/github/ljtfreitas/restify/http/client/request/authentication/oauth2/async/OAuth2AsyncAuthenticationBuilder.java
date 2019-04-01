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
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenMemoryStorage;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeGrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientAuthenticationMethod;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentialsAccessTokenStrategy;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentialsGrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ImplicitGrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ResourceOwner;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ResourceOwnerGrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ResourceOwnerPasswordAccessTokenStrategy;
import com.github.ljtfreitas.restify.spi.Provider;

public class OAuth2AsyncAuthenticationBuilder {

	private OAuth2AsyncAuthenticationGrantTypeBuilder grantTypeBuilder = new OAuth2AsyncAuthenticationGrantTypeBuilder();
	private OAuth2AsyncAuthorizationServerBuilder authorizationServerBuilder = new OAuth2AsyncAuthorizationServerBuilder();

	private AsyncAccessTokenProvider accessTokenProvider;
	private AsyncAccessTokenRepository accessTokenRepository = null;
	private AsyncAccessTokenStorage accessTokenStorage = null;

	public OAuth2AsyncAuthenticationGrantTypeBuilder grantType() {
		return grantTypeBuilder;
	}

	public OAuth2AsyncAuthenticationBuilder repository(AsyncAccessTokenRepository accessTokenRepository) {
		this.accessTokenRepository = accessTokenRepository;
		return this;
	}

	public OAuth2AsyncAuthenticationBuilder storage(AsyncAccessTokenStorage accessTokenStorage) {
		this.accessTokenStorage = accessTokenStorage;
		return this;
	}

	public OAuth2AsyncAuthenticationBuilder provider(AsyncAccessTokenProvider accessTokenProvider) {
		this.accessTokenProvider = accessTokenProvider;
		return this;
	}

	public OAuth2AsyncAuthorizationServerBuilder authorizationServer() {
		return authorizationServerBuilder;
	}

	public OAuth2AsyncAuthentication build() {
		return new OAuth2AsyncAuthentication(grantTypeProperties(), accessTokenRepository(), null);
	}

	private AsyncAccessTokenRepository accessTokenRepository() {
		return Optional.ofNullable(accessTokenRepository)
				.orElseGet(() -> new DefaultAsyncAccessTokenRepository(accessTokenProvider(), accessTokenStorage()));
	}

	private GrantProperties grantTypeProperties() {
		return nonNull(grantTypeBuilder.properties, "Grant parameters must be configured.").build();
	}

	private AsyncAccessTokenProvider accessTokenProvider() {
		return Optional.ofNullable(accessTokenProvider)
				.orElseGet(() -> nonNull(grantTypeBuilder.properties, "Grant parameters must be configured.")
						.createProviderWith(authorizationServerBuilder.build()));
	}

	private AsyncAccessTokenStorage accessTokenStorage() {
		return Optional.ofNullable(accessTokenStorage)
			.orElseGet(() -> new Provider().single(AsyncAccessTokenStorage.class)
				.orElseGet(() -> new AsyncAccessTokenStorageAdapter(new AccessTokenMemoryStorage())));
	}

	public class OAuth2AsyncAuthorizationServerBuilder {

		private AsyncAuthorizationServer authorizationServer;
		private AsyncEndpointRequestExecutor asyncEndpointRequestExecutor;
		private AsyncHttpClientRequestFactory asyncHttpClientRequestFactory;
		private ClientAuthenticationMethod authenticationMethod = ClientAuthenticationMethod.HEADER;

		private final OAuth2AsyncAuthorizationServerHttpMessageConvertersBuilder httpMessageConvertersBuilder = new OAuth2AsyncAuthorizationServerHttpMessageConvertersBuilder();

		public OAuth2AsyncAuthorizationServerBuilder executor(AsyncEndpointRequestExecutor asyncEndpointRequestExecutor) {
			this.asyncEndpointRequestExecutor = asyncEndpointRequestExecutor;
			return this;
		}

		public OAuth2AsyncAuthorizationServerHttpMessageConvertersBuilder converters() {
			return httpMessageConvertersBuilder;
		}

		public OAuth2AsyncAuthorizationServerBuilder converters(HttpMessageConverter... converters) {
			this.httpMessageConvertersBuilder.add(converters);
			return this;
		}

		public OAuth2AsyncAuthorizationServerBuilder client(AsyncHttpClientRequestFactory asyncHttpClientRequestFactory) {
			this.asyncHttpClientRequestFactory = asyncHttpClientRequestFactory;
			return this;
		}

		public OAuth2AsyncAuthorizationServerBuilder method(ClientAuthenticationMethod authenticationMethod) {
			this.authenticationMethod = authenticationMethod;
			return this;
		}

		public OAuth2AsyncAuthenticationBuilder using(AsyncAuthorizationServer asyncAuthorizationServer) {
			this.authorizationServer = asyncAuthorizationServer;
			return OAuth2AsyncAuthenticationBuilder.this;
		}

		public OAuth2AsyncAuthenticationBuilder and() {
			return OAuth2AsyncAuthenticationBuilder.this;
		}

		private AsyncAuthorizationServer build() {
			return Optional.ofNullable(authorizationServer)
					.orElseGet(() -> withEndpointRequestExecutor()
							.orElseGet(() -> withHttpMessageConverters().orElseGet(() -> withHttpClientRequestFactory()
									.orElseGet(() -> new DefaultAsyncAuthorizationServer(authenticationMethod)))));
		}

		private Optional<AsyncAuthorizationServer> withEndpointRequestExecutor() {
			return Optional.ofNullable(asyncEndpointRequestExecutor)
					.map(e -> new DefaultAsyncAuthorizationServer(e, authenticationMethod));
		}

		private Optional<AsyncAuthorizationServer> withHttpMessageConverters() {
			return Optional.ofNullable(httpMessageConvertersBuilder).map(b -> b.build())
					.map(c -> new DefaultAsyncAuthorizationServer(c, authenticationMethod));
		}

		private Optional<AsyncAuthorizationServer> withHttpClientRequestFactory() {
			return Optional.ofNullable(asyncHttpClientRequestFactory)
					.map(f -> new DefaultAsyncAuthorizationServer(f, authenticationMethod));
		}

		public class OAuth2AsyncAuthorizationServerHttpMessageConvertersBuilder {

			private final Collection<HttpMessageConverter> converters = new ArrayList<>(Arrays.asList(
					new TextPlainMessageConverter(), new FormURLEncodedParametersMessageConverter()));

			private final Collection<HttpMessageConverter> customized = new ArrayList<>();

			private final Provider provider = new Provider();

			public OAuth2AsyncAuthorizationServerHttpMessageConvertersBuilder add(HttpMessageConverter... converters) {
				this.customized.addAll(Arrays.asList(converters));
				return this;
			}

			public OAuth2AsyncAuthorizationServerHttpMessageConvertersBuilder json() {
				provider.single(JsonMessageConverter.class).ifPresent(customized::add);
				return this;
			}

			public OAuth2AsyncAuthorizationServerHttpMessageConvertersBuilder xml() {
				provider.single(XmlMessageConverter.class).ifPresent(customized::add);
				return this;
			}

			public OAuth2AsyncAuthenticationBuilder and() {
				return OAuth2AsyncAuthenticationBuilder.this;
			}

			private HttpMessageConverters build() {
				return customized.isEmpty() ? new HttpMessageConverters(json().xml().all()) : new HttpMessageConverters(all());
			}

			private Collection<HttpMessageConverter> all() {
				Collection<HttpMessageConverter> all = new ArrayList<>(converters);
				all.addAll(customized);
				return all;
			}
		}
	}

	public class OAuth2AsyncAuthenticationGrantTypeBuilder {

		private OAuth2AsyncAuthenticationGrantPropertiesBuilder properties = null;

		public OAuth2AsyncClientCredentialsGrantBuilder clientCredentials() {
			properties = new OAuth2AsyncClientCredentialsGrantBuilder();
			return (OAuth2AsyncClientCredentialsGrantBuilder) properties;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder resourceOwner() {
			properties = new OAuth2AsyncResourceOwnerGrantBuilder();
			return (OAuth2AsyncResourceOwnerGrantBuilder) properties;
		}

		public OAuth2AsyncImplicitGrantBuilder implicit() {
			properties = new OAuth2AsyncImplicitGrantBuilder();
			return (OAuth2AsyncImplicitGrantBuilder) properties;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder authorizationCode() {
			properties = new OAuth2AsyncAuthorizationCodeGrantBuilder();
			return (OAuth2AsyncAuthorizationCodeGrantBuilder) properties;
		}
	}

	private abstract class OAuth2AccessTokenStrategyFactory {
		abstract AsyncAccessTokenStrategy createStrategyWith(AsyncAuthorizationServer asyncAuthorizationServer);
	}

	private abstract class OAuth2AsyncAuthenticationGrantPropertiesBuilder extends OAuth2AccessTokenStrategyFactory {
		abstract GrantProperties build();

		AsyncAccessTokenProvider createProviderWith(AsyncAuthorizationServer asyncAuthorizationServer) {
			return new DefaultAsyncAccessTokenProvider(createStrategyWith(asyncAuthorizationServer), asyncAuthorizationServer);
		}
	}

	public class OAuth2AsyncClientCredentialsGrantBuilder extends OAuth2AsyncAuthenticationGrantPropertiesBuilder {

		private final ClientCredentialsGrantProperties.Builder delegate = GrantProperties.Builder.clientCredentials();

		public OAuth2AsyncClientCredentialsGrantBuilder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncClientCredentialsGrantBuilder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncClientCredentialsGrantBuilder accessTokenUri(URL accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncClientCredentialsGrantBuilder credentials(String clientId, String clientSecret) {
			delegate.credentials(clientId, clientSecret);
			return this;
		}

		public OAuth2AsyncClientCredentialsGrantBuilder credentials(ClientCredentials credentials) {
			delegate.credentials(credentials);
			return this;
		}

		public OAuth2AsyncClientCredentialsGrantBuilder scopes(Collection<String> scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2AsyncClientCredentialsGrantBuilder scopes(String... scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2AsyncClientCredentialsGrantBuilder user(Principal user) {
			delegate.user(user);
			return this;
		}

		public OAuth2AsyncAuthenticationBuilder and() {
			return OAuth2AsyncAuthenticationBuilder.this;
		}

		@Override
		protected GrantProperties build() {
			return delegate.build();
		}

		@Override
		protected AsyncAccessTokenStrategy createStrategyWith(AsyncAuthorizationServer asyncAuthorizationServer) {
			return new AsyncAccessTokenStrategyAdapter(new ClientCredentialsAccessTokenStrategy());
		}
	}

	public class OAuth2AsyncResourceOwnerGrantBuilder extends OAuth2AsyncAuthenticationGrantPropertiesBuilder {

		private final ResourceOwnerGrantProperties.Builder delegate = GrantProperties.Builder.resourceOwner();

		public OAuth2AsyncResourceOwnerGrantBuilder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder accessTokenUri(URL accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder clientId(String clientId) {
			delegate.clientId(clientId);
			return this;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder credentials(String clientId, String clientSecret) {
			delegate.credentials(clientId, clientSecret);
			return this;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder credentials(ClientCredentials credentials) {
			delegate.credentials(credentials);
			return this;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder scopes(Collection<String> scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder scopes(String... scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder user(Principal user) {
			delegate.user(user);
			return this;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder resourceOwner(ResourceOwner resourceOwner) {
			delegate.resourceOwner(resourceOwner);
			return this;
		}

		public OAuth2AsyncResourceOwnerGrantBuilder resourceOwner(String username, String password) {
			delegate.resourceOwner(username, password);
			return this;
		}

		public OAuth2AsyncAuthenticationBuilder and() {
			return OAuth2AsyncAuthenticationBuilder.this;
		}

		@Override
		protected GrantProperties build() {
			return delegate.build();
		}

		@Override
		protected AsyncAccessTokenStrategy createStrategyWith(AsyncAuthorizationServer authorizationServer) {
			return new AsyncAccessTokenStrategyAdapter(new ResourceOwnerPasswordAccessTokenStrategy());
		}
	}

	public class OAuth2AsyncImplicitGrantBuilder extends OAuth2AsyncAuthenticationGrantPropertiesBuilder {

		private final ImplicitGrantProperties.Builder delegate = GrantProperties.Builder.implicit();

		public OAuth2AsyncImplicitGrantBuilder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder accessTokenUri(URL accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder clientId(String clientId) {
			delegate.clientId(clientId);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder credentials(String clientId, String clientSecret) {
			delegate.credentials(clientId, clientSecret);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder credentials(ClientCredentials credentials) {
			delegate.credentials(credentials);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder scopes(Collection<String> scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder scopes(String... scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder user(Principal user) {
			delegate.user(user);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder authorizationCode(String authorizationCode) {
			delegate.authorizationCode(authorizationCode);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder authorizationUri(URI authorizationUri) {
			delegate.authorizationUri(authorizationUri);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder authorizationUri(String authorizationUri) {
			delegate.authorizationUri(authorizationUri);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder redirectUri(URI redirectUri) {
			delegate.redirectUri(redirectUri);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder redirectUri(String redirectUri) {
			delegate.redirectUri(redirectUri);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder responseType(String responseType) {
			delegate.responseType(responseType);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder cookie(String cookie) {
			delegate.cookie(cookie);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder cookie(Cookie cookie) {
			delegate.cookie(cookie);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder cookie(String name, String value) {
			delegate.cookie(name, value);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder cookies(Cookies cookies) {
			delegate.cookies(cookies);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder state(String state) {
			delegate.state(state);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder header(String name, String value) {
			delegate.header(name, value);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder header(Header header) {
			delegate.header(header);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder headers(Header... headers) {
			delegate.headers(headers);
			return this;
		}

		public OAuth2AsyncImplicitGrantBuilder headers(Headers headers) {
			delegate.headers(headers);
			return this;
		}

		public OAuth2AsyncAuthenticationBuilder and() {
			return OAuth2AsyncAuthenticationBuilder.this;
		}

		@Override
		protected GrantProperties build() {
			return delegate.build();
		}

		@Override
		protected AsyncAccessTokenStrategy createStrategyWith(AsyncAuthorizationServer authorizationServer) {
			throw new UnsupportedOperationException();
		}

		@Override
		AsyncAccessTokenProvider createProviderWith(AsyncAuthorizationServer authorizationServer) {
			return new AsyncImplicitAccessTokenProvider(authorizationServer);
		}
	}

	public class OAuth2AsyncAuthorizationCodeGrantBuilder extends OAuth2AsyncAuthenticationGrantPropertiesBuilder {

		private final AuthorizationCodeGrantProperties.Builder delegate = GrantProperties.Builder.authorizationCode();

		private AsyncAuthorizationCodeProvider asyncAuthorizationCodeProvider;

		public OAuth2AsyncAuthorizationCodeGrantBuilder accessTokenUri(String accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder accessTokenUri(URI accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder accessTokenUri(URL accessTokenUri) {
			delegate.accessTokenUri(accessTokenUri);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder clientId(String clientId) {
			delegate.clientId(clientId);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder credentials(ClientCredentials credentials) {
			delegate.credentials(credentials);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder credentials(String clientId, String clientSecret) {
			delegate.credentials(clientId, clientSecret);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder scopes(Collection<String> scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder scopes(String... scopes) {
			delegate.scopes(scopes);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder user(Principal user) {
			delegate.user(user);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder authorizationCode(String authorizationCode) {
			delegate.authorizationCode(authorizationCode);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder authorizationUri(URI authorizationUri) {
			delegate.authorizationUri(authorizationUri);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder authorizationUri(String authorizationUri) {
			delegate.authorizationUri(authorizationUri);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder redirectUri(URI redirectUri) {
			delegate.redirectUri(redirectUri);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder redirectUri(String redirectUri) {
			delegate.redirectUri(redirectUri);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder responseType(String responseType) {
			delegate.responseType(responseType);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder cookie(String cookie) {
			delegate.cookie(cookie);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder cookie(Cookie cookie) {
			delegate.cookie(cookie);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder cookie(String name, String value) {
			delegate.cookie(name, value);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder cookies(Cookies cookies) {
			delegate.cookies(cookies);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder state(String state) {
			delegate.state(state);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder header(String name, String value) {
			delegate.header(name, value);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder header(Header header) {
			delegate.header(header);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder headers(Header... headers) {
			delegate.headers(headers);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder headers(Headers headers) {
			delegate.headers(headers);
			return this;
		}

		public OAuth2AsyncAuthorizationCodeGrantBuilder authorizationCodeProvider(
				AsyncAuthorizationCodeProvider asyncAuthorizationCodeProvider) {
			this.asyncAuthorizationCodeProvider = asyncAuthorizationCodeProvider;
			return this;
		}

		public OAuth2AsyncAuthenticationBuilder and() {
			return OAuth2AsyncAuthenticationBuilder.this;
		}

		@Override
		protected GrantProperties build() {
			return delegate.build();
		}

		@Override
		protected AsyncAccessTokenStrategy createStrategyWith(AsyncAuthorizationServer asyncAuthorizationServer) {
			AsyncAuthorizationCodeProvider asyncAuthorizationCodeProvider = Optional.ofNullable(this.asyncAuthorizationCodeProvider)
					.orElseGet(() -> new DefaultAsyncAuthorizationCodeProvider(asyncAuthorizationServer));

			return new AsyncAuthorizationCodeAccessTokenStrategy(asyncAuthorizationCodeProvider);
		}
	}
}
