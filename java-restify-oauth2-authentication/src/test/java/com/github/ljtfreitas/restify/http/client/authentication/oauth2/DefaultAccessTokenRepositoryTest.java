package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.security.Principal;
import java.time.Duration;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAccessTokenRepositoryTest {

	@Mock
	private GrantProperties properties;

	@Mock
	private AccessTokenStorage accessTokenStorage;

	@Mock
	private AccessTokenProvider accessTokenProvider;

	@InjectMocks
	private DefaultAccessTokenRepository accessTokenRepository;

	@Mock
	private Principal user;

	private OAuth2AuthenticatedEndpointRequest request;

	@Before
	public void setup() {
		when(user.getName()).thenReturn("user-identity-key");

		EndpointRequest source = new EndpointRequest(URI.create("http://my.resource.server/path"), "GET");

		request = new OAuth2AuthenticatedEndpointRequest(source, properties, user);
	}

	@Test
	public void shouldGetAccessTokenFromStore() {
		AccessToken accessToken = AccessToken.bearer("access-token");

		when(accessTokenStorage.findBy(request)).thenReturn(Optional.of(accessToken));

		AccessToken output = accessTokenRepository.findBy(request);

		assertSame(accessToken, output);
	}

	@Test
	public void shouldGetAccessTokenFromProviderWhenStoreHasNoToken() {
		AccessToken accessToken = AccessToken.bearer("access-token");

		when(accessTokenStorage.findBy(request)).thenReturn(Optional.empty());
		when(accessTokenProvider.provides(request)).thenReturn(accessToken);

		AccessToken output = accessTokenRepository.findBy(request);

		assertSame(output, accessToken);

		verify(accessTokenProvider).provides(request);
		verify(accessTokenStorage).add(request, accessToken);
	}

	@Test
	public void shouldGetAccessTokenFromProviderWhenStoredTokenHasExpired() throws Exception {
		AccessToken expiredAccessToken = AccessToken.bearer("access-token", Duration.ofMillis(500));
		AccessToken newAccessToken = AccessToken.bearer("new-access-token");

		when(accessTokenStorage.findBy(request)).thenReturn(Optional.of(expiredAccessToken));
		when(accessTokenProvider.provides(request)).thenReturn(newAccessToken);

		Thread.sleep(1000);

		AccessToken output = accessTokenRepository.findBy(request);

		assertSame(newAccessToken, output);

		verify(accessTokenStorage).findBy(request);
		verify(accessTokenProvider).provides(request);
		verify(accessTokenStorage).add(request, newAccessToken);
	}

	@Test
	public void shouldRefreshAccessTokenFromProviderWhenStoredTokenHasExpiredWithRefreshToken() throws Exception {
		AccessToken expiredAccessToken = new AccessToken.Builder(AccessTokenType.BEARER, "access-token")
				.expiration(Duration.ofMillis(500))
				.refreshToken("refresh-token")
				.build();

		AccessToken newAccessToken = AccessToken.bearer("new-access-token");

		when(accessTokenStorage.findBy(request)).thenReturn(Optional.of(expiredAccessToken));
		when(accessTokenProvider.refresh(expiredAccessToken, request)).thenReturn(newAccessToken);

		Thread.sleep(1000);

		AccessToken output = accessTokenRepository.findBy(request);

		assertSame(output, newAccessToken);

		verify(accessTokenStorage).findBy(request);
		verify(accessTokenProvider, never()).provides(request);
		verify(accessTokenProvider).refresh(expiredAccessToken, request);
		verify(accessTokenStorage).add(request, newAccessToken);
	}
}
