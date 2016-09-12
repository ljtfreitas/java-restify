package com.restify.http.client.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.client.EndpointRequest;
import com.restify.http.client.Header;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.converter.HttpMessageReader;

@RunWith(MockitoJUnitRunner.class)
public class AcceptHeaderEndpointRequestInterceptorTest {

	private HttpMessageConverters httpMessageConverters;

	private AcceptHeaderEndpointRequestInterceptor acceptHeaderEndpointRequestInterceptor;

	@Mock
	private HttpMessageReader<Object> httpMessageReaderMock;

	@Before
	public void setup() throws Exception {
		httpMessageConverters = new HttpMessageConverters(httpMessageReaderMock, httpMessageReaderMock, httpMessageReaderMock);

		when(httpMessageReaderMock.canRead(Object.class)).thenReturn(true);
		when(httpMessageReaderMock.contentType()).thenReturn("text/plain", "application/json", "application/xml");

		acceptHeaderEndpointRequestInterceptor = new AcceptHeaderEndpointRequestInterceptor(httpMessageConverters);
	}

	@Test
	public void shouldBuildAcceptHeaderWithAllSupportedContentTypes() throws Exception {
		EndpointRequest endpointRequestObjectExpected = acceptHeaderEndpointRequestInterceptor
				.intercepts(new EndpointRequest(new URI("http://my.api.com/object"), "GET", Object.class));

		Optional<Header> acceptHeader = endpointRequestObjectExpected.headers().get("Accept");

		assertTrue(acceptHeader.isPresent());

		assertEquals("text/plain, application/json, application/xml", acceptHeader.get().value());
	}

	@Test
	public void shouldNotCreateBuildAcceptHeaderWithAllSupportedContentTypes() throws Exception {
		when(httpMessageReaderMock.canRead(Object.class)).thenReturn(false);

		EndpointRequest endpointRquest = acceptHeaderEndpointRequestInterceptor
				.intercepts(new EndpointRequest(new URI("http://my.api.com/integer"), "GET", Integer.class));

		Optional<Header> acceptHeader = endpointRquest.headers().get("Accept");

		assertFalse(acceptHeader.isPresent());
	}
}
