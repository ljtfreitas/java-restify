package com.github.ljtfreitas.restify.http.client.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class EmptyOnNotFoundEndpointResponseErrorFallbackTest2 {

	@Mock
	private EndpointResponseErrorFallback delegate;

	@InjectMocks
	private EmptyOnNotFoundEndpointResponseErrorFallback fallback;

	@Mock
	private HttpResponseMessage response;

	private JavaType responseType;
	
	@Before
	public void before() {
		responseType = JavaType.of(String.class);
	}
	
	@Test
	public void shouldReturnEmptyEndpointResponseWhenHttpResponseIsNotFound() {
		when(response.status()).thenReturn(StatusCode.notFound());

		EndpointResponse<Object> newEndpointResponse = fallback.onError(response, responseType);

		assertEquals(response.status(), newEndpointResponse.status());
		assertSame(response.headers(), newEndpointResponse.headers());
		assertNull(newEndpointResponse.body());
	}
	
	@Test
	public void shouldCallDelegateWhenHttpResponseNotIsNotFound() {
		when(response.status()).thenReturn(StatusCode.badGateway());

		EndpointResponse<Object> responseOfDelegate = EndpointResponse.of(StatusCode.ok(), "delete fallback");
		when(delegate.onError(response, responseType)).thenReturn(responseOfDelegate);

		EndpointResponse<Object> newEndpointResponse = fallback.onError(response, responseType);

		assertSame(responseOfDelegate, newEndpointResponse);
	}
}
