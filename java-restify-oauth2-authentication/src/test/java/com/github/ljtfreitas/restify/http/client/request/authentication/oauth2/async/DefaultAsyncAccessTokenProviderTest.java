package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenResponse;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenResponseBody;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAsyncAccessTokenProviderTest {

	@Mock
	private AsyncAccessTokenStrategy accessTokenStrategy;

	@Mock
	private AsyncAuthorizationServer authorizationServer;

	@InjectMocks
	private DefaultAsyncAccessTokenProvider provider;

	@Mock
	private AccessTokenRequest accessTokenRequest;

	private AccessTokenResponseBody accessTokenResponseBody;

	private AccessToken accessToken;

	private OAuth2AuthenticatedEndpointRequest authenticatedRequest;

	@Before
	public void setup() {
		EndpointRequest source = new EndpointRequest(URI.create("http://my.resource.server/path"), "GET");

		authenticatedRequest = new OAuth2AuthenticatedEndpointRequest(source, GrantProperties.empty());

		accessTokenResponseBody = new AccessTokenResponseBody.Builder()
				.type("Bearer")
				.token("abc123")
				.build();

		accessToken = AccessToken.of(accessTokenResponseBody);
	}

	@Test
	public void shouldGetAccessToken() {
		when(accessTokenStrategy.newAccessTokenRequest(authenticatedRequest))
			.thenReturn(CompletableFuture.completedFuture(accessTokenRequest));

		when(authorizationServer.requireToken(accessTokenRequest))
			.thenReturn(CompletableFuture.completedFuture(new AccessTokenResponse(EndpointResponse.of(StatusCode.ok(), accessTokenResponseBody))));

		CompletableFuture<AccessToken> newAccessTokenAsFuture = provider.provides(authenticatedRequest).toCompletableFuture();

		AccessToken newAccessToken = newAccessTokenAsFuture.join();

		assertEquals(accessToken.type(), newAccessToken.type());
		assertEquals(accessToken.token(), newAccessToken.token());

		verify(accessTokenStrategy).newAccessTokenRequest(authenticatedRequest);
		verify(authorizationServer).requireToken(accessTokenRequest);
	}

	@Test
	public void shouldRefreshToken() {
		AccessTokenResponseBody refreshedAccessTokenResponseBody = new AccessTokenResponseBody.Builder()
				.type("Bearer")
				.token("aaa111")
				.refreshToken("bbb222")
					.build();

		AccessToken refreshedAccessToken = AccessToken.of(refreshedAccessTokenResponseBody);

		when(accessTokenStrategy.newRefreshTokenRequest(refreshedAccessToken, authenticatedRequest))
			.thenReturn(CompletableFuture.completedFuture(accessTokenRequest));

		when(authorizationServer.requireToken(accessTokenRequest))
			.thenReturn(CompletableFuture.completedFuture(new AccessTokenResponse(EndpointResponse.of(StatusCode.ok(), refreshedAccessTokenResponseBody))));

		CompletableFuture<AccessToken> newAccessTokenAsFuture = provider.refresh(refreshedAccessToken, authenticatedRequest).toCompletableFuture();

		AccessToken newAccessToken = newAccessTokenAsFuture.join();

		assertEquals(refreshedAccessToken.type(), newAccessToken.type());
		assertEquals(refreshedAccessToken.token(), newAccessToken.token());
	}
}
