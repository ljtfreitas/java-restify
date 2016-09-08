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
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;

@RunWith(MockitoJUnitRunner.class)
public class AcceptHeaderRequestInterceptorTest {

	private HttpMessageConverters httpMessageConverters;

	private AcceptHeaderRequestInterceptor acceptHeaderEndpointRequestInterceptor;

	@Mock
	private HttpMessageConverter<String> httpTextMessageConverterMock;

	@Mock
	private HttpMessageConverter<Object> httpJsonMessageConverterMock;

	@Mock
	private HttpMessageConverter<Object> httpXmlMessageConverterMock;

	@Before
	public void setup() throws Exception {
		httpMessageConverters = new HttpMessageConverters(httpTextMessageConverterMock, httpJsonMessageConverterMock,
				httpXmlMessageConverterMock);

		when(httpTextMessageConverterMock.readerOf(String.class)).thenReturn(true);
		when(httpTextMessageConverterMock.contentType()).thenReturn("text/plain");

		when(httpJsonMessageConverterMock.contentType()).thenReturn("application/json");
		when(httpJsonMessageConverterMock.readerOf(Object.class)).thenReturn(true);

		when(httpXmlMessageConverterMock.readerOf(Object.class)).thenReturn(true);
		when(httpXmlMessageConverterMock.contentType()).thenReturn("application/xml");

		acceptHeaderEndpointRequestInterceptor = new AcceptHeaderRequestInterceptor(httpMessageConverters);
	}

	@Test
	public void shouldBuildAcceptHeaderWithSingleSupportedContentType() throws Exception {
		EndpointRequest endpointRequestStringExpected = acceptHeaderEndpointRequestInterceptor
				.intercepts(new EndpointRequest(new URI("http://my.api.com/string"), "GET", String.class));

		Optional<Header> acceptHeader = endpointRequestStringExpected.headers().get("Accept");

		assertTrue(acceptHeader.isPresent());

		assertEquals("text/plain", acceptHeader.get().value());
	}

	@Test
	public void shouldBuildAcceptHeaderWithAllSupportedContentTypes() throws Exception {
		EndpointRequest endpointRequestObjectExpected = acceptHeaderEndpointRequestInterceptor
				.intercepts(new EndpointRequest(new URI("http://my.api.com/object"), "GET", Object.class));

		Optional<Header> acceptHeader = endpointRequestObjectExpected.headers().get("Accept");

		assertTrue(acceptHeader.isPresent());

		assertEquals("application/json, application/xml", acceptHeader.get().value());
	}

	@Test
	public void shouldNotCreateBuildAcceptHeaderWithAllSupportedContentTypes() throws Exception {
		EndpointRequest endpointRquest = acceptHeaderEndpointRequestInterceptor.intercepts(new EndpointRequest(new URI("http://my.api.com/integer"), "GET", Integer.class));

		Optional<Header> acceptHeader = endpointRquest.headers().get("Accept");

		assertFalse(acceptHeader.isPresent());
	}
}
