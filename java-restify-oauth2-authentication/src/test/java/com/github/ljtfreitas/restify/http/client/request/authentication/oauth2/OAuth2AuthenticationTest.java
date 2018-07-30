package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestMetadata;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenRepository;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2Authentication;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.Scope;

@RunWith(MockitoJUnitRunner.class)
public class OAuth2AuthenticationTest {

	@Mock
	private AccessTokenRepository accessTokenRepository;

	private AccessToken accessToken;

	private OAuth2Authentication authentication;

	@Captor
	private ArgumentCaptor<OAuth2AuthenticatedEndpointRequest> requestCapture;

	private GrantProperties properties;

	@Spy
	private EndpointRequest source = new EndpointRequest(URI.create("http://my.protected.api"), "GET");

	@Before
	public void setup() {
		accessToken = AccessToken.bearer("aaa111");

		when(accessTokenRepository.findBy(notNull(OAuth2AuthenticatedEndpointRequest.class)))
			.thenReturn(accessToken);

		properties = GrantProperties.Builder.clientCredentials()
				.credentials(new ClientCredentials("client-id", "client-secret"))
				.accessTokenUri("http://my.authorization.server")
				.scopes("read", "write")
				.build();

		authentication = new OAuth2Authentication(properties, accessTokenRepository);
	}

	@Test
	public void shouldGenerateAccessTokenFromOAuthAuthenticatedRequest() {
		String result = authentication.content(source);

		assertEquals(accessToken.toString(), result);

		verify(accessTokenRepository).findBy(notNull(OAuth2AuthenticatedEndpointRequest.class));
	}

	@Test
	public void shouldCreateOAuthAuthenticatedRequestFromEndpointRequestSource() {
		Scope scope = new Scope() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Scope.class;
			}

			@Override
			public String[] value() {
				return new String[]{"custom-scope"};
			}
		};
		doReturn(new EndpointRequestMetadata(Arrays.asList(scope))).when(source).metadata();

		assertNotNull(authentication.content(source));

		verify(accessTokenRepository).findBy(requestCapture.capture());

		OAuth2AuthenticatedEndpointRequest request = requestCapture.getValue();

		assertEquals(properties.accessTokenUri(), request.accessTokenUri());
		assertEquals(properties.credentials(), request.credentials());
		assertEquals(source.endpoint(), request.endpoint());
		assertEquals("read write custom-scope", request.scope());
	}
}
