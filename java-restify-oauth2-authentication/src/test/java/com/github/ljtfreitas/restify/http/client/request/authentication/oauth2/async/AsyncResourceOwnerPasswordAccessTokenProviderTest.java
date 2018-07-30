package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.ParameterBody.params;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenType;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ResourceOwnerGrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ResourceOwnerPasswordAccessTokenStrategy;

@RunWith(MockitoJUnitRunner.class)
public class AsyncResourceOwnerPasswordAccessTokenProviderTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8088);

	private MockServerClient mockServerClient;

	private AsyncAccessTokenProvider provider;

	private String authorizationCredentials;

	private OAuth2AuthenticatedEndpointRequest request;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8088);

		ResourceOwnerGrantProperties properties = GrantProperties.Builder.resourceOwner()
				.accessTokenUri("http://localhost:8088/oauth/token")
				.credentials(new ClientCredentials("client-id", "client-secret"))
				.scopes("read", "write")
				.resourceOwner("my-username", "my-password")
				.build();

		EndpointRequest source = new EndpointRequest(URI.create("http://my.resource.server/path"), "GET");

		request = new OAuth2AuthenticatedEndpointRequest(source, properties);

		provider = new DefaultAsyncAccessTokenProvider(new AsyncAccessTokenStrategyAdapter(new ResourceOwnerPasswordAccessTokenStrategy()));

		authorizationCredentials = "Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ="; //(base64(client_id:client_secret))
	}

	@Test
	public void shouldGetAccessToken() {
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

		CompletableFuture<AccessToken> accessTokenAsFuture = provider.provides(request).toCompletableFuture();

		AccessToken accessToken = accessTokenAsFuture.join();

		assertEquals(AccessTokenType.BEARER, accessToken.type());
		assertEquals("aaa111", accessToken.token());

		assertEquals("read write", accessToken.scope());

		Instant expectedExpiration = Instant.now().plus(Duration.ofSeconds(3600));
		assertEquals(expectedExpiration.truncatedTo(ChronoUnit.SECONDS), accessToken.expiration().get().truncatedTo(ChronoUnit.SECONDS));
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

		AccessToken accessToken = AccessToken.Builder
				.bearer("aaa111")
					.refreshToken("bbb222")
					.build();

		CompletableFuture<AccessToken> newAccessTokenAsFuture = provider.refresh(accessToken, request).toCompletableFuture();

		AccessToken newAccessToken = newAccessTokenAsFuture.join();

		assertEquals(AccessTokenType.BEARER, newAccessToken.type());
		assertEquals("ccc333", newAccessToken.token());

		assertEquals("read write", newAccessToken.scope());

		Instant expectedExpiration = Instant.now().plus(Duration.ofSeconds(3600));
		assertEquals(expectedExpiration.truncatedTo(ChronoUnit.SECONDS), newAccessToken.expiration().get().truncatedTo(ChronoUnit.SECONDS));
	}
}
