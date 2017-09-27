package com.github.ljtfreitas.restify.http.client.request.interceptor.authentication;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.interceptor.authentication.AuthenticationEndpoinRequestInterceptor;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationEndpoinRequestInterceptorTest {

	@Mock
	private Authentication authenticationMock;

	@InjectMocks
	private AuthenticationEndpoinRequestInterceptor interceptor;

	@Test
	public void shouldCreateAuthorizationHeader() {
		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://my.api.com"), "GET");

		when(authenticationMock.content(endpointRequest))
			.thenReturn("abc:123");

		EndpointRequest authorizedEndpointRequest = interceptor.intercepts(endpointRequest);

		assertNotSame(endpointRequest, authorizedEndpointRequest);

		Optional<Header> authorizationHeader = authorizedEndpointRequest.headers().get("Authorization");

		assertTrue(authorizationHeader.isPresent());
		assertEquals("abc:123", authorizationHeader.get().value());
	}

	@Test
	public void shouldReturnSameRequestWhenAuthenticatonReturnNullString() {
		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://my.api.com"), "GET");

		when(authenticationMock.content(endpointRequest))
			.thenReturn(null);

		EndpointRequest unauthorizedEndpointRequest = interceptor.intercepts(endpointRequest);

		assertSame(endpointRequest, unauthorizedEndpointRequest);
	}
}
