package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenType;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentialsAccessTokenStrategy;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;

@RunWith(MockitoJUnitRunner.class)
public class AsyncClientCredentialsAccessTokenProviderTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8088);

	private MockServerClient mockServerClient;

	private AsyncAccessTokenProvider provider;

	private String authorizationCredentials;

	private OAuth2AuthenticatedEndpointRequest request;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

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

		provider = new DefaultAsyncAccessTokenProvider(new AsyncAccessTokenStrategyAdapter(new ClientCredentialsAccessTokenStrategy()));

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

		CompletableFuture<AccessToken> accessTokenAsFuture = provider.provides(request).toCompletableFuture();

		AccessToken accessToken = accessTokenAsFuture.join();

		assertEquals(AccessTokenType.BEARER, accessToken.type());
		assertEquals("aaa111", accessToken.token());

		assertEquals("read write", accessToken.scope());

		Instant expectedExpiration = Instant.now().plus(Duration.ofSeconds(3600));
		assertEquals(expectedExpiration.truncatedTo(ChronoUnit.SECONDS), accessToken.expiration().get().truncatedTo(ChronoUnit.SECONDS));
	}

	@Test
	public void shouldThrowExceptionOnTryRefreshToken() {
		expectedException.expectCause(isA(UnsupportedOperationException.class));

		CompletableFuture<AccessToken> future = provider.refresh(AccessToken.bearer("aaa111"), request).toCompletableFuture();

		assertTrue(future.isCompletedExceptionally());

		future.join();
	}
}
