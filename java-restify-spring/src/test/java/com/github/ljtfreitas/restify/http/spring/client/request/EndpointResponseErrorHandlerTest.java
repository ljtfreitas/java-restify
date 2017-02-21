package com.github.ljtfreitas.restify.http.spring.client.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;

@RunWith(MockitoJUnitRunner.class)
public class EndpointResponseErrorHandlerTest {

	@Mock
	private EndpointResponseErrorFallback endpointErrorFallbackMock;

	@InjectMocks
	private EndpointResponseErrorHandler endpointResponseErrorTranslator;

	@Captor
	private ArgumentCaptor<HttpResponseMessage> httpResponseMessageCaptor;

	@Test
	public void shouldTranslateSpringResponseUsingEndpointResponseErrorFallback() throws Exception {
		ClientHttpResponse response = new MockClientHttpResponse("response body".getBytes(), HttpStatus.BAD_REQUEST);
		response.getHeaders().add("Content-Type", "text/plain");

		endpointResponseErrorTranslator.handleError(response);

		verify(endpointErrorFallbackMock).onError(httpResponseMessageCaptor.capture());

		HttpResponseMessage httpResponseMessage = httpResponseMessageCaptor.getValue();

		assertEquals(StatusCode.badRequest(), httpResponseMessage.statusCode());

		assertTrue(httpResponseMessage.headers().get("Content-Type").isPresent());
		assertEquals("text/plain", httpResponseMessage.headers().get("Content-Type").get().value());

		BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponseMessage.body()));
		assertEquals("response body", reader.readLine());
	}
}
