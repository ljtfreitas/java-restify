package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.security.Principal;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenStorageKey;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenType;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAsyncAccessTokenRepositoryTest {

	@Mock
	private GrantProperties properties;

	@Mock
	private AsyncAccessTokenStorage accessTokenStorage;

	@Mock
	private AsyncAccessTokenProvider accessTokenProvider;

	@InjectMocks
	private DefaultAsyncAccessTokenRepository accessTokenRepository;

	@Mock
	private Principal user;

	private OAuth2AuthenticatedEndpointRequest request;

	@Before
	public void setup() {
		when(user.getName()).thenReturn("user-identity-key");
		when(properties.credentials()).thenReturn(ClientCredentials.clientId("client-id"));

		when(accessTokenStorage.add(notNull(AccessTokenStorageKey.class), notNull(AccessToken.class)))
			.then(i -> CompletableFuture.completedFuture(null));

		EndpointRequest source = new EndpointRequest(URI.create("http://my.resource.server/path"), "GET");

		request = new OAuth2AuthenticatedEndpointRequest(source, properties, user);
	}

	@Test
	public void shouldGetAccessTokenFromStore() {
		AccessToken accessToken = AccessToken.bearer("access-token");

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(CompletableFuture.completedFuture(Optional.of(accessToken)));

		CompletableFuture<AccessToken> output = accessTokenRepository.findBy(request).toCompletableFuture();

		assertSame(accessToken, output.join());
	}

	@Test
	public void shouldGetAccessTokenFromProviderWhenStoreHasNoToken() {
		AccessToken accessToken = AccessToken.bearer("access-token");

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(CompletableFuture.completedFuture(Optional.empty()));

		when(accessTokenProvider.provides(request))
			.thenReturn(CompletableFuture.completedFuture(accessToken));

		CompletableFuture<AccessToken> output = accessTokenRepository.findBy(request).toCompletableFuture();

		assertSame(accessToken, output.join());

		verify(accessTokenProvider).provides(request);
		verify(accessTokenStorage).add(notNull(AccessTokenStorageKey.class), eq(accessToken));
	}

	@Test
	public void shouldGetAccessTokenFromProviderWhenStoredTokenHasExpired() throws Exception {
		AccessToken expiredAccessToken = AccessToken.bearer("access-token", Duration.ofMillis(500));
		AccessToken newAccessToken = AccessToken.bearer("new-access-token");

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(CompletableFuture.completedFuture(Optional.of(expiredAccessToken)));

		when(accessTokenProvider.provides(request))
			.thenReturn(CompletableFuture.completedFuture(newAccessToken));

		Thread.sleep(1000);

		CompletableFuture<AccessToken> output = accessTokenRepository.findBy(request).toCompletableFuture();

		assertSame(newAccessToken, output.join());

		verify(accessTokenStorage).findBy(notNull(AccessTokenStorageKey.class));
		verify(accessTokenProvider).provides(request);
		verify(accessTokenStorage).add(notNull(AccessTokenStorageKey.class), eq(newAccessToken));
	}

	@Test
	public void shouldRefreshAccessTokenFromProviderWhenStoredTokenHasExpiredWithRefreshToken() throws Exception {
		AccessToken expiredAccessToken = new AccessToken.Builder(AccessTokenType.BEARER, "access-token")
				.expiration(Duration.ofMillis(500))
				.refreshToken("refresh-token")
				.build();

		AccessToken newAccessToken = AccessToken.bearer("new-access-token");

		when(accessTokenStorage.findBy(notNull(AccessTokenStorageKey.class)))
			.thenReturn(CompletableFuture.completedFuture(Optional.of(expiredAccessToken)));

		when(accessTokenProvider.refresh(expiredAccessToken, request))
			.thenReturn(CompletableFuture.completedFuture(newAccessToken));

		Thread.sleep(1000);

		CompletableFuture<AccessToken> output = accessTokenRepository.findBy(request).toCompletableFuture();

		assertSame(newAccessToken, output.join());

		verify(accessTokenStorage).findBy(notNull(AccessTokenStorageKey.class));
		verify(accessTokenProvider, never()).provides(request);
		verify(accessTokenProvider).refresh(expiredAccessToken, request);
		verify(accessTokenStorage).add(notNull(AccessTokenStorageKey.class), eq(newAccessToken));
	}
}
