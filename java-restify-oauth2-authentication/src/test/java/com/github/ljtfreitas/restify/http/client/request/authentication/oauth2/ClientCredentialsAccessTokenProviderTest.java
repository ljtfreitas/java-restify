package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.ParameterBody.params;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;

@RunWith(MockitoJUnitRunner.class)
public class ClientCredentialsAccessTokenProviderTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8088);

	private MockServerClient mockServerClient;

	private ClientCredentialsAccessTokenProvider provider;

	private String authorizationCredentials;

	private OAuth2AuthenticatedEndpointRequest request;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8088);

		GrantProperties properties = GrantProperties.Builder.clientCredentials()
				.accessTokenUri("http://localhost:8088/oauth/token")
				.credentials(new ClientCredentials("client-id", "client-secret"))
				.scopes("read", "write")
				.build();

		EndpointRequest source = new EndpointRequest(URI.create("http://my.resource.server/path"), "GET");

		request = new OAuth2AuthenticatedEndpointRequest(source, properties);

		provider = new ClientCredentialsAccessTokenProvider();

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

		AccessToken accessToken = provider.provides(request);

		assertEquals(AccessTokenType.BEARER, accessToken.type());
		assertEquals("aaa111", accessToken.token());

		assertEquals("read write", accessToken.scope());

		Instant expectedExpiration = Instant.now().plus(Duration.ofSeconds(3600));
		assertEquals(expectedExpiration.truncatedTo(ChronoUnit.SECONDS), accessToken.expiration().get().truncatedTo(ChronoUnit.SECONDS));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldThrowExceptionOnTryRefreshToken() {
		provider.refresh(AccessToken.bearer("aaa111"), request);
	}
}
