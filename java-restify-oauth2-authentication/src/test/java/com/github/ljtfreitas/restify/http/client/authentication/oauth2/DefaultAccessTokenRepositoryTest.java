package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.Duration;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAccessTokenRepositoryTest {

	@Mock
	private Resource resource;

	@Mock
	private AccessTokenStore accessTokenStore;

	@Mock
	private AccessTokenProvider accessTokenProvider;

	@InjectMocks
	private DefaultAccessTokenRepository accessTokenRepository;

	@Mock
	private Principal user;

	@Before
	public void setup() {
		when(user.getName()).thenReturn("user-identity-key");
	}

	@Test
	public void shouldGetAccessTokenFromStore() {
		AccessToken accessToken = AccessToken.bearer("access-token");

		when(accessTokenStore.findBy(user, resource)).thenReturn(Optional.of(accessToken));

		AccessToken output = accessTokenRepository.findBy(user, resource);

		assertSame(output, accessToken);
	}

	@Test
	public void shouldGetAccessTokenFromProviderWhenStoreHasNoToken() {
		AccessToken accessToken = AccessToken.bearer("access-token");

		when(accessTokenStore.findBy(user, resource)).thenReturn(Optional.empty());
		when(accessTokenProvider.provides()).thenReturn(accessToken);

		AccessToken output = accessTokenRepository.findBy(user, resource);

		assertSame(output, accessToken);

		verify(accessTokenProvider).provides();
		verify(accessTokenStore).add(user, resource, accessToken);
	}

	@Test
	public void shouldGetAccessTokenFromProviderWhenStoredTokenHasExpired() throws Exception {
		AccessToken expiredAccessToken = AccessToken.bearer("access-token", Duration.ofMillis(500));
		AccessToken newAccessToken = AccessToken.bearer("new-access-token");

		when(accessTokenStore.findBy(user, resource)).thenReturn(Optional.of(expiredAccessToken));
		when(accessTokenProvider.provides()).thenReturn(newAccessToken);

		Thread.sleep(1000);

		AccessToken output = accessTokenRepository.findBy(user, resource);

		assertSame(output, newAccessToken);

		verify(accessTokenStore).findBy(user, resource);
		verify(accessTokenProvider).provides();
		verify(accessTokenStore).add(user, resource, newAccessToken);
	}
}
