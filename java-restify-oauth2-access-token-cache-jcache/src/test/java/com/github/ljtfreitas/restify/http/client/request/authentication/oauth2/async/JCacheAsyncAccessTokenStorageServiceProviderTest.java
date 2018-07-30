package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;

import com.github.ljtfreitas.restify.spi.Provider;

public class JCacheAsyncAccessTokenStorageServiceProviderTest {

	@Test
	public void shouldDiscoveryJCacheAsyncAccessTokenStorageService() {
		Provider provider = new Provider();

		Optional<AsyncAccessTokenStorage> service = provider.single(AsyncAccessTokenStorage.class);

		assertThat(service.isPresent(), is(true));
		assertThat(service.get(), instanceOf(JCacheAsyncAccessTokenStorage.class));
	}
}
