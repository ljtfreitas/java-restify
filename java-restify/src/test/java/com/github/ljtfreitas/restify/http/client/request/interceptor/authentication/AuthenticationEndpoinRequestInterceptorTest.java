package com.github.ljtfreitas.restify.http.client.request.interceptor.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.authentication.Authentication;
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
		when(authenticationMock.content())
			.thenReturn("abc:123");

		EndpointRequest endpointRequest = new EndpointRequest(URI.create("http://my.api.com"), "GET");

		interceptor.intercepts(endpointRequest);

		Optional<Header> authorizationHeader = endpointRequest.headers().get("Authorization");

		assertTrue(authorizationHeader.isPresent());
		assertEquals("abc:123", authorizationHeader.get().value());
	}

}
