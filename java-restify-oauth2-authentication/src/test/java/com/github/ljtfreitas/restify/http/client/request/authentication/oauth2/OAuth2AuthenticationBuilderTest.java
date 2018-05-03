package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

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
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenProvider;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenRepository;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenStorage;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeProvider;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationServer;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2Authentication;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticationBuilder;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

public class OAuth2AuthenticationBuilderTest {

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
	public void shouldBuildOAuth2AuthenticationObjectToClientCredentialsGrantType() {
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

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
				.grantType()
					.clientCredentials()
						.credentials(clientCredentials)
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.build();

		assertNotNull(authentication);

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToClientCredentialsGrantTypeUsingCustomizedAccessTokenRepository() {
		AccessTokenRepository accessTokenRepository = mock(AccessTokenRepository.class);

		when(accessTokenRepository.findBy(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(AccessToken.bearer("aaa111"));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
				.grantType()
					.clientCredentials()
						.credentials(clientCredentials)
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.repository(accessTokenRepository)
				.build();

		assertNotNull(authentication);

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenRepository).findBy(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToClientCredentialsGrantTypeUsingCustomizedAccessTokenStorage() {
		AccessTokenStorage accessTokenStorage = mock(AccessTokenStorage.class);

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(Optional.ofNullable(AccessToken.bearer("aaa111")));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
				.grantType()
					.clientCredentials()
						.credentials(clientCredentials)
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.storage(accessTokenStorage)
				.build();

		assertNotNull(authentication);

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenStorage).findBy(notNull(AccessTokenStorageKey.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToClientCredentialsGrantTypeUsingCustomizedAccessTokenProvider() {
		AccessTokenProvider accessTokenProvider = mock(AccessTokenProvider.class);

		when(accessTokenProvider.provides(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(AccessToken.bearer("aaa111"));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
				.grantType()
					.clientCredentials()
						.credentials(clientCredentials)
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.provider(accessTokenProvider)
				.build();

		assertNotNull(authentication);

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenProvider).provides(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToClientCredentialsGrantTypeUsingCustomizedAuthorizationServer() {
		AuthorizationServer authorizationServer = mock(AuthorizationServer.class);

		when(authorizationServer.requireToken(notNull(AccessTokenRequest.class)))
			.thenReturn(new EndpointResponse<>(StatusCode.ok(), new Headers(), AccessToken.bearer("aaa111")));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(authorizationServer).requireToken(notNull(AccessTokenRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToResourceOwnerGrantType() {
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

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
				.grantType()
					.resourceOwner()
						.credentials(clientCredentials)
						.resourceOwner("my-username", "my-password")
						.accessTokenUri("http://localhost:8085/oauth/token")
						.scopes("read", "write")
					.and()
				.build();

		assertNotNull(authentication);

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToResourceOwnerGrantTypeUsingCustomizedAccessTokenRepository() {
		AccessTokenRepository accessTokenRepository = mock(AccessTokenRepository.class);

		when(accessTokenRepository.findBy(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(AccessToken.bearer("aaa111"));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenRepository).findBy(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToResourceOwnerGrantTypeUsingCustomizedAccessTokenStorage() {
		AccessTokenStorage accessTokenStorage = mock(AccessTokenStorage.class);

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(Optional.ofNullable(AccessToken.bearer("aaa111")));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenStorage).findBy(notNull(AccessTokenStorageKey.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToResourceOwnerGrantTypeUsingCustomizedAccessTokenProvider() {
		AccessTokenProvider accessTokenProvider = mock(AccessTokenProvider.class);

		when(accessTokenProvider.provides(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(AccessToken.bearer("aaa111"));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenProvider).provides(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToResourceOwnerGrantTypeUsingCustomizedAuthorizationServer() {
		AuthorizationServer authorizationServer = mock(AuthorizationServer.class);

		when(authorizationServer.requireToken(notNull(AccessTokenRequest.class)))
			.thenReturn(new EndpointResponse<>(StatusCode.ok(), new Headers(), AccessToken.bearer("aaa111")));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(authorizationServer).requireToken(notNull(AccessTokenRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToImplicitGrantType() {
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

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToImplicitGrantTypeUsingCustomizedAccessTokenRepository() {
		AccessTokenRepository accessTokenRepository = mock(AccessTokenRepository.class);

		when(accessTokenRepository.findBy(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(AccessToken.bearer("aaa111"));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenRepository).findBy(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToImplicitGrantTypeUsingCustomizedAccessTokenStorage() {
		AccessTokenStorage accessTokenStorage = mock(AccessTokenStorage.class);

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(Optional.ofNullable(AccessToken.bearer("aaa111")));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenStorage).findBy(notNull(AccessTokenStorageKey.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToImplicitGrantTypeUsingCustomizedAccessTokenProvider() {
		AccessTokenProvider accessTokenProvider = mock(AccessTokenProvider.class);

		when(accessTokenProvider.provides(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(AccessToken.bearer("aaa111"));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenProvider).provides(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToImplicitGrantTypeUsingCustomizedAuthorizationServer() {
		AuthorizationServer authorizationServer = mock(AuthorizationServer.class);

		Header location = new Header("Location", "http://my.web.app/oauth/callback#access_token=aaa111&token_type=bearer&state=current-state&expires_in=3600&scope=read%20write");
		EndpointResponse<String> response = new EndpointResponse<>(StatusCode.found(), new Headers(location), "hello");

		when(authorizationServer.authorize(notNull(AuthorizationCodeRequest.class))).thenReturn(response);

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(authorizationServer).authorize(notNull(AuthorizationCodeRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToAuthorizationCodeGrantType() {
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

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToAuthorizationCodeGrantTypeUsingCustomizedAccessTokenRepository() {
		AccessTokenRepository accessTokenRepository = mock(AccessTokenRepository.class);

		when(accessTokenRepository.findBy(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(AccessToken.bearer("aaa111"));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenRepository).findBy(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToAuthorizationCodeGrantTypeUsingCustomizedAccessTokenStorage() {
		AccessTokenStorage accessTokenStorage = mock(AccessTokenStorage.class);

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(Optional.ofNullable(AccessToken.bearer("aaa111")));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(accessTokenStorage).findBy(notNull(AccessTokenStorageKey.class));
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToAuthorizationCodeGrantTypeUsingCustomizedAuthorizationServer() {
		AuthorizationServer authorizationServer = mock(AuthorizationServer.class);

		Header location = new Header("Location", "http://my.web.app/oauth/callback?code=abc1234&state=current-state");
		EndpointResponse<String> response = new EndpointResponse<>(StatusCode.found(), new Headers(location), "hello");
		when(authorizationServer.authorize(notNull(AuthorizationCodeRequest.class)))
			.thenReturn(response);

		when(authorizationServer.requireToken(notNull(AccessTokenRequest.class)))
			.thenReturn(new EndpointResponse<>(StatusCode.ok(), new Headers(), AccessToken.bearer("aaa111")));

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);
	}

	@Test
	public void shouldBuildOAuth2AuthenticationObjectToAuthorizationCodeGrantTypeUsingCustomizedAuthorizationCodeProvider() {
		AuthorizationCodeProvider authorizationCodeProvider = mock(AuthorizationCodeProvider.class);

		when(authorizationCodeProvider.provides(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn("abc1234");

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

		OAuth2Authentication authentication = new OAuth2AuthenticationBuilder()
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

		String content = authentication.content(new EndpointRequest(URI.create("http://my.api.com"), "GET"));

		assertEquals("Bearer aaa111", content);

		verify(authorizationCodeProvider).provides(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}
}
