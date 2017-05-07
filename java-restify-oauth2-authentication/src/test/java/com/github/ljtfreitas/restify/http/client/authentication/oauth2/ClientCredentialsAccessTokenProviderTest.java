package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.assertEquals;
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
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

@RunWith(MockitoJUnitRunner.class)
public class ClientCredentialsAccessTokenProviderTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8088);

	private MockServerClient mockServerClient;

	private ClientCredentialsAccessTokenProvider provider;

	private String authorizationCredentials;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8088);

		OAuth2Configuration configuration = new OAuth2Configuration.Builder()
				.accessTokenUri("http://localhost:8088/oauth/token")
				.credentials(new OAuth2ClientCredentials("client-id", "client-secret"))
				.scopes("read", "write")
				.build();

		provider = new ClientCredentialsAccessTokenProvider(configuration);

		authorizationCredentials = "Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ="; //(base64(client_id:client_secret))
	}

	@Test
	public void shouldGetAccessToken() {
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

		OAuth2AccessToken accessToken = provider.provides();

		assertEquals(OAuth2AccessTokenType.BEARER, accessToken.type());
		assertEquals("aaa111", accessToken.token());

		assertEquals("read write", accessToken.scope());

		LocalDateTime expectedExpiration = LocalDateTime.now().plusSeconds(3600);
		assertEquals(expectedExpiration.truncatedTo(ChronoUnit.SECONDS), accessToken.expiration().truncatedTo(ChronoUnit.SECONDS));
	}
}
