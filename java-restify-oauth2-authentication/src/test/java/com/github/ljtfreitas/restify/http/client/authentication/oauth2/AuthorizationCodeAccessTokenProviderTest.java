package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.ParameterBody.params;

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
	private DefaultAuthorizationCodeProvider authorizationCodeProvider;

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
				.authorizationCode("abc1234")
				.build();

		provider = new AuthorizationCodeAccessTokenProvider(configuration);

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
					.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\"}")));

		OAuth2AccessToken accessToken = provider.get();

		assertEquals(OAuth2AccessTokenType.BEARER, accessToken.type());
		assertEquals("aaa111", accessToken.token());
	}
}
