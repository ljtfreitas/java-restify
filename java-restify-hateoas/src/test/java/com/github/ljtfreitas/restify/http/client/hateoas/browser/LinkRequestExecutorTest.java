package com.github.ljtfreitas.restify.http.client.hateoas.browser;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

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
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

@RunWith(MockitoJUnitRunner.class)
public class LinkRequestExecutorTest {

	@Mock
	private AsyncEndpointRequestExecutor asyncEndpointRequestExecutor;

	@InjectMocks
	private LinkRequestExecutor linkRequestExecutor;

	@Captor
	private ArgumentCaptor<EndpointRequest> endpointRequestCaptor;

	@Test
	public void shouldExecuteEndpointRequestFromLink() {
		Link link = Link.self("http://localhost:8080/me");
		LinkEndpointRequest linkEndpointRequest = new LinkEndpointRequest(link, String.class);

		EndpointResponse<Object> expectedEndpointResponse = new EndpointResponse<>(StatusCode.ok(), new Headers(), "hello");

		when(asyncEndpointRequestExecutor.executeAsync(notNull(EndpointRequest.class)))
			.thenReturn(CompletableFuture.completedFuture(expectedEndpointResponse));

		CompletableFuture<EndpointResponse<Object>> response = linkRequestExecutor.execute(linkEndpointRequest)
				.toCompletableFuture();

		assertSame(expectedEndpointResponse, response.join());

		verify(asyncEndpointRequestExecutor).executeAsync(notNull(EndpointRequest.class));
	}

}
