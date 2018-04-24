package com.github.ljtfreitas.restify.http.netflix.client.request.discovery;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.SocketException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.netflix.client.request.DefaultRibbonHttpClientRequest;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonRequest;

@RunWith(MockitoJUnitRunner.class)
public class ServiceFailureExceptionHandlerTest {

	@Mock
	private ServiceInstanceFailureHandler serviceInstanceFailureHandlerMock;

	@Mock
	private DefaultRibbonHttpClientRequest ribbonHttpClientRequestMock;

	private ServiceFailureExceptionHandler handler;

	private RibbonRequest ribbonRequest;

	@Captor
	private ArgumentCaptor<ServiceInstance> serviceInstanceCaptor;

	@Before
	public void setup() {
		when(ribbonHttpClientRequestMock.loadBalancedEndpoint()).thenReturn(URI.create("http://address:8080/path"));

		ribbonRequest = new RibbonRequest(ribbonHttpClientRequestMock);
	}

	@Test
	public void shouldCallOnConnectionFailureWhenExceptionCauseIsSocketException() {
		handler = new ServiceFailureExceptionHandler(serviceInstanceFailureHandlerMock);

		Exception exception = new Exception("Oh!", new SocketException());

		handler.onException(ribbonRequest, exception);

		verify(serviceInstanceFailureHandlerMock).onFailure(serviceInstanceCaptor.capture(), same(exception));

		ServiceInstance failureInstance = serviceInstanceCaptor.getValue();

		assertEquals(ribbonRequest.serviceName(), failureInstance.name());
		assertEquals(ribbonRequest.getUri().getHost(), failureInstance.host());
		assertEquals(ribbonRequest.getUri().getPort(), failureInstance.port());
	}

	@Test
	public void shouldCallOnConnectionFailureWhenExceptionCauseIsParameterizedCauseException() {
		Collection<Class<? extends Throwable>> causes = Arrays.asList(WhateverException.class);

		handler = new ServiceFailureExceptionHandler(serviceInstanceFailureHandlerMock, causes);

		Exception exception = new Exception("Oh!", new WhateverException());

		handler.onException(ribbonRequest, exception);

		verify(serviceInstanceFailureHandlerMock).onFailure(serviceInstanceCaptor.capture(), same(exception));

		ServiceInstance failureInstance = serviceInstanceCaptor.getValue();

		assertEquals(ribbonRequest.serviceName(), failureInstance.name());
		assertEquals(ribbonRequest.getUri().getHost(), failureInstance.host());
		assertEquals(ribbonRequest.getUri().getPort(), failureInstance.port());
	}

	@Test
	public void shouldNotCallOnConnectionFailureWhenExceptionCauseNotIsAFailureException() {
		Collection<Class<? extends Throwable>> causes = Arrays.asList(WhateverException.class);

		handler = new ServiceFailureExceptionHandler(serviceInstanceFailureHandlerMock, causes);

		Exception exception = new Exception("Oh!", new IllegalArgumentException());

		handler.onException(ribbonRequest, exception);

		verify(serviceInstanceFailureHandlerMock, never()).onFailure(any(ServiceInstance.class), same(exception));
	}

	@SuppressWarnings("serial")
	private class WhateverException extends RuntimeException {
	}
}
