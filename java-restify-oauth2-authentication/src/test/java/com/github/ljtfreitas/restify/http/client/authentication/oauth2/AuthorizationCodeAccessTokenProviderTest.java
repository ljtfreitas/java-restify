package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.ParameterBody.params;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationCodeAccessTokenProviderTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8088);

	private MockServerClient mockServerClient;

	@Mock
	private DefaultOAuth2AuthorizationCodeProvider authorizationCodeProvider;

	private AuthorizationCodeAccessTokenProvider provider;

	private String authorizationCredentials;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8088);

		OAuth2AuthorizationConfiguration configuration = new OAuth2AuthorizationConfiguration.Builder()
				.authorizationUri("http://localhost:8088/oauth/authorize")
				.accessTokenUri("http://localhost:8088/oauth/token")
				.credentials(new OAuth2ClientCredentials("client-id", "client-secret"))
				.redirectUri("http://my.web.app/oauth/callback")
				.scopes("read", "write")
				.build();

		when(authorizationCodeProvider.provides()).thenReturn("abc1234");

		provider = new AuthorizationCodeAccessTokenProvider(configuration, authorizationCodeProvider);

		authorizationCredentials = "Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ="; //(base64(client_id:client_secret))
	}

	@Test
	public void shouldGetAccessToken() {
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

		OAuth2AccessToken accessToken = provider.provides();

		assertEquals(OAuth2AccessTokenType.BEARER, accessToken.type());
		assertEquals("aaa111", accessToken.token());

		assertEquals("read write", accessToken.scope());

		LocalDateTime expectedExpiration = LocalDateTime.now().plusSeconds(3600);
		assertEquals(expectedExpiration.truncatedTo(ChronoUnit.SECONDS), accessToken.expiration().truncatedTo(ChronoUnit.SECONDS));

		verify(authorizationCodeProvider).provides();
	}

	@Test
	public void shouldRefreshToken() {
		mockServerClient
			.when(request()
					.withMethod("POST")
					.withPath("/oauth/token")
					.withBody(params(new Parameter("grant_type", "refresh_token"),
									 new Parameter("refresh_token", "bbb222")))
					.withHeader("Authorization", "Basic " + authorizationCredentials))
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"access_token\":\"ccc333\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"read write\"}")));

		OAuth2AccessToken accessToken = OAuth2AccessToken.Builder
				.bearer("aaa111")
					.refreshToken("bbb222")
					.build();

		OAuth2AccessToken newAccessToken = provider.refresh(accessToken);

		assertEquals(OAuth2AccessTokenType.BEARER, newAccessToken.type());
		assertEquals("ccc333", newAccessToken.token());

		assertEquals("read write", newAccessToken.scope());

		LocalDateTime expectedExpiration = LocalDateTime.now().plusSeconds(3600);
		assertEquals(expectedExpiration.truncatedTo(ChronoUnit.SECONDS), newAccessToken.expiration().truncatedTo(ChronoUnit.SECONDS));
	}
}
