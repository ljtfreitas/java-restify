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

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

public class OAuth2AccessTokenMemoryStoreTest {

	private OAuth2AccessTokenMemoryStore acessTokenMemoryStore;

	@Test
	public void shouldSaveAccessTokenOnStore() {
		OAuth2DelegateUser user = () -> "user-identity-key";

		OAuth2Configuration configuration = new OAuth2Configuration.Builder()
				.resourceKey("my-resource-server")
				.credentials(OAuth2ClientCredentials.clientId("client-id"))
				.scopes("read", "write")
				.build();

		acessTokenMemoryStore = new OAuth2AccessTokenMemoryStore();

		assertFalse(acessTokenMemoryStore.findBy(user, configuration).isPresent());

		OAuth2AccessToken source = OAuth2AccessToken.bearer("accessToken");

		acessTokenMemoryStore.add(user, configuration, source);

		Optional<OAuth2AccessToken> foundAccessToken = acessTokenMemoryStore.findBy(user, configuration);

		assertTrue(foundAccessToken.isPresent());
		assertSame(source, foundAccessToken.get());
	}
}
