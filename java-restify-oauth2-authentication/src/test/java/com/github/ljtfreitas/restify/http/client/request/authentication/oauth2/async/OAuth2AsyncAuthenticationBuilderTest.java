package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.ParameterBody.params;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenResponse;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenResponseBody;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenStorageKey;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeResponse;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

public class OAuth2AsyncAuthenticationBuilderTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8085);

	private MockServerClient mockServerClient;

	private ClientCredentials clientCredentials;

	private String authorizationCredentials;

	@Before
	public void setup() {
		clientCredentials = new ClientCredentials("client_id", "client_secret");

		authorizationCredentials = "Y2xpZW50X2lkOmNsaWVudF9zZWNyZXQ="; // (base64(client_id:client_secret))
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToClientCredentialsGrantType() {
		mockServerClient
			.when(request()
					.withMethod("POST")
					.withPath("/oauth/token")
					.withBody(params(new Parameter("grant_type", "client_credentials"),
									 new Parameter("scope", "read write")))
					.withHeader("Authorization", "Basic " + authorizationCredentials))
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"read write\"}")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.clientCredentials()
						.credentials(clientCredentials)
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET"))
				.toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToClientCredentialsGrantTypeUsingCustomizedAccessTokenRepository() {
		AsyncAccessTokenRepository accessTokenRepository = mock(AsyncAccessTokenRepository.class);

		when(accessTokenRepository.findBy(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(AccessToken.bearer("aaa111")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.clientCredentials()
						.credentials(clientCredentials)
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.repository(accessTokenRepository)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET"))
				.toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenRepository).findBy(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToClientCredentialsGrantTypeUsingCustomizedAccessTokenStorage() {
		AsyncAccessTokenStorage accessTokenStorage = mock(AsyncAccessTokenStorage.class);

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(CompletableFuture.completedFuture(Optional.ofNullable(AccessToken.bearer("aaa111"))));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.clientCredentials()
						.credentials(clientCredentials)
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.storage(accessTokenStorage)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenStorage).findBy(notNull(AccessTokenStorageKey.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToClientCredentialsGrantTypeUsingCustomizedAccessTokenProvider() {
		AsyncAccessTokenProvider accessTokenProvider = mock(AsyncAccessTokenProvider.class);

		when(accessTokenProvider.provides(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(AccessToken.bearer("aaa111")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.clientCredentials()
						.credentials(clientCredentials)
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.provider(accessTokenProvider)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenProvider).provides(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToClientCredentialsGrantTypeUsingCustomizedAuthorizationServer() {
		AsyncAuthorizationServer authorizationServer = mock(AsyncAuthorizationServer.class);

		AccessTokenResponseBody accessTokenResponseBody = new AccessTokenResponseBody.Builder().type("Bearer").token("aaa111").build();

		when(authorizationServer.requireToken(notNull(AccessTokenRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(new AccessTokenResponse(new EndpointResponse<>(StatusCode.ok(), accessTokenResponseBody))));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.clientCredentials()
						.credentials(clientCredentials)
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.authorizationServer()
					.using(authorizationServer)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(authorizationServer).requireToken(notNull(AccessTokenRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToResourceOwnerGrantType() {
		mockServerClient
			.when(request()
					.withMethod("POST")
					.withPath("/oauth/token")
					.withBody(params(new Parameter("grant_type", "password"),
							 new Parameter("scope", "read write"),
							 new Parameter("username", "my-username"),
							 new Parameter("password", "my-password")))
					.withHeader("Authorization", "Basic " + authorizationCredentials))
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"read write\"}")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.resourceOwner()
						.credentials(clientCredentials)
						.resourceOwner("my-username", "my-password")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToResourceOwnerGrantTypeUsingCustomizedAccessTokenRepository() {
		AsyncAccessTokenRepository accessTokenRepository = mock(AsyncAccessTokenRepository.class);

		when(accessTokenRepository.findBy(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(AccessToken.bearer("aaa111")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.resourceOwner()
						.credentials(clientCredentials)
						.resourceOwner("my-username", "my-password")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.repository(accessTokenRepository)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET"))
				.toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenRepository).findBy(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToResourceOwnerGrantTypeUsingCustomizedAccessTokenStorage() {
		AsyncAccessTokenStorage accessTokenStorage = mock(AsyncAccessTokenStorage.class);

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(CompletableFuture.completedFuture(Optional.ofNullable(AccessToken.bearer("aaa111"))));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.resourceOwner()
						.credentials(clientCredentials)
						.resourceOwner("my-username", "my-password")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.storage(accessTokenStorage)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenStorage).findBy(notNull(AccessTokenStorageKey.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToResourceOwnerGrantTypeUsingCustomizedAccessTokenProvider() {
		AsyncAccessTokenProvider accessTokenProvider = mock(AsyncAccessTokenProvider.class);

		when(accessTokenProvider.provides(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(AccessToken.bearer("aaa111")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.resourceOwner()
						.credentials(clientCredentials)
						.resourceOwner("my-username", "my-password")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.provider(accessTokenProvider)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenProvider).provides(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToResourceOwnerGrantTypeUsingCustomizedAuthorizationServer() {
		AsyncAuthorizationServer authorizationServer = mock(AsyncAuthorizationServer.class);

		AccessTokenResponseBody accessTokenResponseBody = new AccessTokenResponseBody.Builder().type("Bearer").token("aaa111").build();

		when(authorizationServer.requireToken(notNull(AccessTokenRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(new AccessTokenResponse(new EndpointResponse<>(StatusCode.ok(), accessTokenResponseBody))));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.resourceOwner()
						.credentials(clientCredentials)
						.resourceOwner("my-username", "my-password")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.authorizationServer()
					.using(authorizationServer)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(authorizationServer).requireToken(notNull(AccessTokenRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToImplicitGrantType() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/oauth/authorize")
				.withQueryStringParameters(new Parameter("response_type", "token"),
										   new Parameter("client_id", "client_id"),
										   new Parameter("redirect_uri", "http://my.web.app/oauth/callback"),
										   new Parameter("scope", "read write")))
			.respond(response()
				.withStatusCode(302)
				.withHeader("Content-Type", "application/x-www-form-urlencoded")
				.withHeader("Location", "http://my.web.app/oauth/callback#access_token=aaa111&token_type=bearer&state=current-state&expires_in=3600&scope=read%20write"));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.implicit()
						.authorizationUri("http://localhost:8085/oauth/authorize")
						.clientId("client_id")
						.redirectUri("http://my.web.app/oauth/callback")
						.scopes("read", "write")
						.responseType("token")
						.state("current-state")
					.and()
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToImplicitGrantTypeUsingCustomizedAccessTokenRepository() {
		AsyncAccessTokenRepository accessTokenRepository = mock(AsyncAccessTokenRepository.class);

		when(accessTokenRepository.findBy(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(AccessToken.bearer("aaa111")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.implicit()
						.authorizationUri("http://localhost:8085/oauth/authorize")
						.clientId("client_id")
						.redirectUri("http://my.web.app/oauth/callback")
						.scopes("read", "write")
						.responseType("token")
						.state("current-state")
					.and()
				.repository(accessTokenRepository)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenRepository).findBy(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToImplicitGrantTypeUsingCustomizedAccessTokenStorage() {
		AsyncAccessTokenStorage accessTokenStorage = mock(AsyncAccessTokenStorage.class);

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(CompletableFuture.completedFuture(Optional.ofNullable(AccessToken.bearer("aaa111"))));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.implicit()
						.authorizationUri("http://localhost:8085/oauth/authorize")
						.clientId("client_id")
						.redirectUri("http://my.web.app/oauth/callback")
						.scopes("read", "write")
						.responseType("token")
						.state("current-state")
					.and()
				.storage(accessTokenStorage)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenStorage).findBy(notNull(AccessTokenStorageKey.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToImplicitGrantTypeUsingCustomizedAccessTokenProvider() {
		AsyncAccessTokenProvider accessTokenProvider = mock(AsyncAccessTokenProvider.class);

		when(accessTokenProvider.provides(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(AccessToken.bearer("aaa111")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.implicit()
						.authorizationUri("http://localhost:8085/oauth/authorize")
						.clientId("client_id")
						.redirectUri("http://my.web.app/oauth/callback")
						.scopes("read", "write")
						.responseType("token")
						.state("current-state")
					.and()
				.provider(accessTokenProvider)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET"))
				.toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenProvider).provides(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToImplicitGrantTypeUsingCustomizedAuthorizationServer() {
		AsyncAuthorizationServer authorizationServer = mock(AsyncAuthorizationServer.class);

		Header location = new Header("Location", "http://my.web.app/oauth/callback#access_token=aaa111&token_type=bearer&state=current-state&expires_in=3600&scope=read%20write");
		AuthorizationCodeResponse response = new AuthorizationCodeResponse(new EndpointResponse<>(StatusCode.found(), new Headers(location),  "hello"));

		when(authorizationServer.authorize(notNull(AuthorizationCodeRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(response));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.implicit()
						.authorizationUri("http://localhost:8085/oauth/authorize")
						.clientId("client-id")
						.redirectUri("http://my.web.app/oauth/callback")
						.scopes("read", "write")
						.responseType("token")
						.state("current-state")
					.and()
				.authorizationServer()
					.using(authorizationServer)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET"))
				.toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(authorizationServer).authorize(notNull(AuthorizationCodeRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToAuthorizationCodeGrantType() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/oauth/authorize")
				.withQueryStringParameters(new Parameter("response_type", "token"),
										   new Parameter("client_id", "client_id"),
										   new Parameter("redirect_uri", "http://my.web.app/oauth/callback"),
										   new Parameter("scope", "read write")))
			.respond(response()
				.withStatusCode(302)
				.withHeader("Content-Type", "application/x-www-form-urlencoded")
				.withHeader("Location", "http://my.web.app/oauth/callback?code=abc1234&state=current-state"));

		mockServerClient
			.when(request()
					.withMethod("POST")
					.withPath("/oauth/token")
					.withBody(params(new Parameter("grant_type", "authorization_code"),
									 new Parameter("code", "abc1234"),
									 new Parameter("redirect_uri", "http://my.web.app/oauth/callback")))
					.withHeader("Authorization", "Basic " + authorizationCredentials))
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"read write\"}")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.authorizationCode()
						.authorizationUri("http://localhost:8085/oauth/authorize")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.credentials("client_id", "client_secret")
						.redirectUri("http://my.web.app/oauth/callback")
						.scopes("read", "write")
						.responseType("token")
						.state("current-state")
					.and()
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET"))
				.toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToAuthorizationCodeGrantTypeUsingCustomizedAccessTokenRepository() {
		AsyncAccessTokenRepository accessTokenRepository = mock(AsyncAccessTokenRepository.class);

		when(accessTokenRepository.findBy(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(AccessToken.bearer("aaa111")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.authorizationCode()
						.authorizationUri("http://localhost:8085/oauth/authorize")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.credentials("client_id", "client_secret")
						.redirectUri("http://my.web.app/oauth/callback")
						.scopes("read", "write")
						.responseType("token")
						.state("current-state")
					.and()
				.repository(accessTokenRepository)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET"))
				.toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenRepository).findBy(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToAuthorizationCodeGrantTypeUsingCustomizedAccessTokenStorage() {
		AsyncAccessTokenStorage accessTokenStorage = mock(AsyncAccessTokenStorage.class);

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(CompletableFuture.completedFuture(Optional.ofNullable(AccessToken.bearer("aaa111"))));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.authorizationCode()
						.authorizationUri("http://localhost:8085/oauth/authorize")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.credentials("client_id", "client_secret")
						.redirectUri("http://my.web.app/oauth/callback")
						.scopes("read", "write")
						.responseType("token")
						.state("current-state")
					.and()
				.storage(accessTokenStorage)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET"))
				.toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(accessTokenStorage).findBy(notNull(AccessTokenStorageKey.class));
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToAuthorizationCodeGrantTypeUsingCustomizedAuthorizationServer() {
		AsyncAuthorizationServer authorizationServer = mock(AsyncAuthorizationServer.class);

		Header location = new Header("Location", "http://my.web.app/oauth/callback?code=abc1234&state=current-state");
		AuthorizationCodeResponse response = new AuthorizationCodeResponse(new EndpointResponse<>(StatusCode.found(), new Headers(location), "hello"));
		when(authorizationServer.authorize(notNull(AuthorizationCodeRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(response));

		AccessTokenResponseBody accessTokenResponseBody = new AccessTokenResponseBody.Builder().type("Bearer").token("aaa111").build();
		when(authorizationServer.requireToken(notNull(AccessTokenRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(new AccessTokenResponse(new EndpointResponse<>(StatusCode.ok(), new Headers(), accessTokenResponseBody))));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.authorizationCode()
						.authorizationUri("http://localhost:8085/oauth/authorize")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.credentials("client_id", "client_secret")
						.redirectUri("http://my.web.app/oauth/callback")
						.scopes("read", "write")
						.responseType("token")
						.state("current-state")
					.and()
				.authorizationServer()
					.using(authorizationServer)
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET")).toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());
	}

	@Test
	public void shouldBuildOAuth2AsyncAuthenticationObjectToAuthorizationCodeGrantTypeUsingCustomizedAuthorizationCodeProvider() {
		AsyncAuthorizationCodeProvider authorizationCodeProvider = mock(AsyncAuthorizationCodeProvider.class);

		when(authorizationCodeProvider.provides(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(CompletableFuture.completedFuture("abc1234"));

		mockServerClient
			.when(request()
				.withMethod("POST")
				.withPath("/oauth/token")
				.withBody(params(new Parameter("grant_type", "authorization_code"),
								 new Parameter("code", "abc1234"),
								 new Parameter("redirect_uri", "http://my.web.app/oauth/callback")))
				.withHeader("Authorization", "Basic " + authorizationCredentials))
			.respond(response()
				.withStatusCode(200)
				.withHeader("Content-Type", "application/json")
				.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"read write\"}")));

		OAuth2AsyncAuthentication authentication = new OAuth2AsyncAuthenticationBuilder()
				.grantType()
					.authorizationCode()
						.authorizationUri("http://localhost:8085/oauth/authorize")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.credentials("client_id", "client_secret")
						.redirectUri("http://my.web.app/oauth/callback")
						.scopes("read", "write")
						.responseType("token")
						.state("current-state")
						.authorizationCodeProvider(authorizationCodeProvider)
					.and()
				.build();

		assertNotNull(authentication);

		CompletableFuture<String> content = authentication.contentAsync(new EndpointRequest(URI.create("http://my.api.com"), "GET"))
				.toCompletableFuture();

		assertEquals("Bearer aaa111", content.join());

		verify(authorizationCodeProvider).provides(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}
}
