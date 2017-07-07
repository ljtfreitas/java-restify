package com.github.ljtfreitas.restify.http.netflix.client.request.zookeeper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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

import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonHttpClientRequest;
import com.github.ljtfreitas.restify.http.netflix.client.request.RibbonRequest;

@RunWith(MockitoJUnitRunner.class)
public class ZookeeperServiceFailureExceptionHandlerTest {

	@Mock
	private ZookeeperServiceDiscovery<?> zookeeperServiceDiscoveryMock;

	@Mock
	private RibbonHttpClientRequest ribbonHttpClientRequestMock;

	private ZookeeperServiceFailureExceptionHandler handler;

	private RibbonRequest ribbonRequest;

	@Captor
	private ArgumentCaptor<ZookeeperServiceInstance> zookeeperServiceInstanceCaptor;

	@Before
	public void setup() {
		when(ribbonHttpClientRequestMock.ribbonEndpoint()).thenReturn(URI.create("http://address:8080/path"));

		ribbonRequest = new RibbonRequest(ribbonHttpClientRequestMock);
	}

	@Test
	public void shouldCallOnConnectionFailureWhenExceptionCauseIsSocketException() {
		handler = new ZookeeperServiceFailureExceptionHandler(zookeeperServiceDiscoveryMock);

		Exception exception = new Exception("Oh!", new SocketException());

		handler.onException(ribbonRequest, exception);

		verify(zookeeperServiceDiscoveryMock).onFailure(zookeeperServiceInstanceCaptor.capture());

		ZookeeperServiceInstance failureInstance = zookeeperServiceInstanceCaptor.getValue();

		assertEquals(ribbonRequest.serviceName(), failureInstance.name());
		assertEquals(ribbonRequest.getUri().getHost(), failureInstance.address());
		assertEquals(ribbonRequest.getUri().getPort(), failureInstance.port());
	}

	@Test
	public void shouldCallOnConnectionFailureWhenExceptionCauseIsParameterizedCauseException() {
		Collection<Class<? extends Throwable>> causes = Arrays.asList(WhateverException.class);

		handler = new ZookeeperServiceFailureExceptionHandler(zookeeperServiceDiscoveryMock, causes);

		Exception exception = new Exception("Oh!", new WhateverException());

		handler.onException(ribbonRequest, exception);

		verify(zookeeperServiceDiscoveryMock).onFailure(zookeeperServiceInstanceCaptor.capture());

		ZookeeperServiceInstance failureInstance = zookeeperServiceInstanceCaptor.getValue();

		assertEquals(ribbonRequest.serviceName(), failureInstance.name());
		assertEquals(ribbonRequest.getUri().getHost(), failureInstance.address());
		assertEquals(ribbonRequest.getUri().getPort(), failureInstance.port());
	}

	@Test
	public void shouldNotCallOnConnectionFailureWhenExceptionCauseNotIsAFailureException() {
		Collection<Class<? extends Throwable>> causes = Arrays.asList(WhateverException.class);

		handler = new ZookeeperServiceFailureExceptionHandler(zookeeperServiceDiscoveryMock, causes);

		Exception exception = new Exception("Oh!", new IllegalArgumentException());

		handler.onException(ribbonRequest, exception);

		verify(zookeeperServiceDiscoveryMock, never()).onFailure(any(ZookeeperServiceInstance.class));
	}

	@SuppressWarnings("serial")
	private class WhateverException extends RuntimeException {
	}
}
