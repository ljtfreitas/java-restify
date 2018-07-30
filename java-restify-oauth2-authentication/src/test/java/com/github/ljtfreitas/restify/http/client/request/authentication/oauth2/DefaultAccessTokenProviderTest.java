package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAccessTokenProviderTest {

	@Mock
	private AccessTokenStrategy accessTokenStrategy;

	@Mock
	private AuthorizationServer authorizationServer;

	@InjectMocks
	private DefaultAccessTokenProvider provider;

	@Mock
	private AccessTokenRequest accessTokenRequest;

	private AccessTokenResponseBody accessTokenResponseBody;

	private AccessToken accessToken;

	private OAuth2AuthenticatedEndpointRequest authenticatedRequest;

	@Before
	public void setup() {
		EndpointRequest source = new EndpointRequest(URI.create("http://my.resource.server/path"), "GET");

		authenticatedRequest = new OAuth2AuthenticatedEndpointRequest(source, new GrantProperties());

		accessTokenResponseBody = new AccessTokenResponseBody.Builder()
				.type("Bearer")
				.token("abc123")
				.build();

		accessToken = AccessToken.of(accessTokenResponseBody);
	}

	@Test
	public void shouldGetAccessToken() {
		when(accessTokenStrategy.newAccessTokenRequest(authenticatedRequest))
			.thenReturn(accessTokenRequest);

		when(authorizationServer.requireToken(accessTokenRequest))
			.thenReturn(new AccessTokenResponse(new EndpointResponse<>(StatusCode.ok(), accessTokenResponseBody)));

		AccessToken newAccessToken = provider.provides(authenticatedRequest);

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
			.thenReturn(accessTokenRequest);

		when(authorizationServer.requireToken(accessTokenRequest))
			.thenReturn(new AccessTokenResponse(new EndpointResponse<>(StatusCode.ok(), refreshedAccessTokenResponseBody)));

		AccessToken newAccessToken = provider.refresh(refreshedAccessToken, authenticatedRequest);

		assertEquals(refreshedAccessToken.type(), newAccessToken.type());
		assertEquals(refreshedAccessToken.token(), newAccessToken.token());
	}
}
