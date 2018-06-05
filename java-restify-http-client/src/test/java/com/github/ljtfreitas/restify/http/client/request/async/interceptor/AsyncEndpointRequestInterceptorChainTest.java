package com.github.ljtfreitas.restify.http.client.request.async.interceptor;

import static org.junit.Assert.assertSame;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;

@RunWith(MockitoJUnitRunner.class)
public class AsyncEndpointRequestInterceptorChainTest {

	@Mock
	private AsyncEndpointRequestInterceptor interceptor;

	@SuppressWarnings("unchecked")
	@Test
	public void shouldInterceptsEndpointRequestUsingAsync() {
		when(interceptor.interceptsAsync(notNull(CompletableFuture.class)))
			.then(returnsFirstArg());

		AsyncEndpointRequestInterceptorChain chain = new AsyncEndpointRequestInterceptorChain(Arrays.asList(interceptor));

		EndpointRequest request = new EndpointRequest(URI.create("http://whatever"), "GET");

		CompletableFuture<EndpointRequest> intercepted = chain.applyAsync(request);

		assertSame(request, intercepted.join());

		verify(interceptor).interceptsAsync(notNull(CompletableFuture.class));
	}

	@Test
	public void shouldAdaptSyncInterceptorToAsync() {
		EndpointRequestInterceptor syncInterceptor = mock(EndpointRequestInterceptor.class);

		when(syncInterceptor.intercepts(notNull(EndpointRequest.class)))
			.then(returnsFirstArg());

		AsyncEndpointRequestInterceptorChain chain = AsyncEndpointRequestInterceptorChain.of(Arrays.asList(syncInterceptor));

		EndpointRequest request = new EndpointRequest(URI.create("http://whatever"), "GET");

		CompletableFuture<EndpointRequest> intercepted = chain.applyAsync(request);

		assertSame(request, intercepted.join());

		verify(syncInterceptor).intercepts(request);
	}
}
