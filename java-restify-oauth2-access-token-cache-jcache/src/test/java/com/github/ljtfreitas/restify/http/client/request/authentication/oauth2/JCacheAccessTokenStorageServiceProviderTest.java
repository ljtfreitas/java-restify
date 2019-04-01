package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;

import com.github.ljtfreitas.restify.spi.Provider;

public class JCacheAccessTokenStorageServiceProviderTest {

	@Test
	public void shouldDiscoveryCaffeineAccessTokenStorageService() {
		Provider provider = new Provider();

		Optional<AccessTokenStorage> service = provider.single(AccessTokenStorage.class);

		assertThat(service.isPresent(), is(true));
		assertThat(service.get(), instanceOf(JCacheAccessTokenStorage.class));
	}
}
