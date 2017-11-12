package com.github.ljtfreitas.restify.http.client.hateoas.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

@RunWith(MockitoJUnitRunner.class)
public class LinkRequestExecutorTest {

	@Mock
	private EndpointRequestExecutor endpointRequestExecutor;

	@Mock
	private EndpointRequestInterceptorStack interceptorStack;

	@InjectMocks
	private LinkRequestExecutor linkRequestExecutor;

	@Captor
	private ArgumentCaptor<EndpointRequest> endpointRequestCaptor;

	@Test
	public void shouldExecuteEndpointRequestFromLink() {
		Link link = Link.self("http://localhost:8080/me");
		LinkEndpointRequest linkEndpointRequest = new LinkEndpointRequest(link, String.class);

		EndpointResponse<Object> expectedEndpointResponse = new EndpointResponse<>(StatusCode.ok(), new Headers(), "hello");

		when(interceptorStack.apply(notNull(EndpointRequest.class))).then(returnsFirstArg());
		when(endpointRequestExecutor.execute(notNull(EndpointRequest.class))).thenReturn(expectedEndpointResponse);

		EndpointResponse<String> response = linkRequestExecutor.execute(linkEndpointRequest);

		assertSame(expectedEndpointResponse, response);

		verify(interceptorStack).apply(endpointRequestCaptor.capture());

		EndpointRequest endpointRequest = endpointRequestCaptor.getValue();

		assertEquals(link.href(), endpointRequest.endpoint().toString());
		assertEquals("GET", endpointRequest.method());
		assertEquals(String.class, endpointRequest.responseType().unwrap());

		verify(endpointRequestExecutor).execute(endpointRequest);
	}

}
