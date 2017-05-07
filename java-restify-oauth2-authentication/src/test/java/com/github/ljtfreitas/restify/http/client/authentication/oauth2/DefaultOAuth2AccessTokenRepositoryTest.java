/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultOAuth2AccessTokenRepositoryTest {

	@Mock
	private OAuth2Configuration configuration;

	@Mock
	private OAuth2AccessTokenStore accessTokenStore;

	@Mock
	private OAuth2AccessTokenProvider accessTokenProvider;

	@InjectMocks
	private DefaultOAuth2AccessTokenRepository accessTokenRepository;

	@Mock
	private OAuth2DelegateUser user;

	@Before
	public void setup() {
		when(user.identity()).thenReturn("user-identity-key");
	}

	@Test
	public void shouldGetAccessTokenFromStore() {
		OAuth2AccessToken accessToken = OAuth2AccessToken.bearer("access-token");

		when(accessTokenStore.findBy(user, configuration)).thenReturn(Optional.of(accessToken));

		OAuth2AccessToken output = accessTokenRepository.findBy(user, configuration);

		assertSame(output, accessToken);
	}

	@Test
	public void shouldGetAccessTokenFromProviderWhenStoreHasNoToken() {
		OAuth2AccessToken accessToken = OAuth2AccessToken.bearer("access-token");

		when(accessTokenStore.findBy(user, configuration)).thenReturn(Optional.empty());
		when(accessTokenProvider.provides()).thenReturn(accessToken);

		OAuth2AccessToken output = accessTokenRepository.findBy(user, configuration);

		assertSame(output, accessToken);

		verify(accessTokenProvider).provides();
		verify(accessTokenStore).add(user, configuration, accessToken);
	}

	@Test
	public void shouldGetAccessTokenFromProviderWhenStoredTokenHasExpired() throws Exception {
		OAuth2AccessToken expiredAccessToken = OAuth2AccessToken.bearer("access-token", Duration.ofMillis(500));
		OAuth2AccessToken newAccessToken = OAuth2AccessToken.bearer("new-access-token");

		when(accessTokenStore.findBy(user, configuration)).thenReturn(Optional.of(expiredAccessToken));
		when(accessTokenProvider.provides()).thenReturn(newAccessToken);

		Thread.sleep(1000);

		OAuth2AccessToken output = accessTokenRepository.findBy(user, configuration);

		assertSame(output, newAccessToken);

		verify(accessTokenStore).findBy(user, configuration);
		verify(accessTokenProvider).provides();
		verify(accessTokenStore).add(user, configuration, newAccessToken);
	}
}
