package com.github.ljtfreitas.restify.http.client.request.async.interceptor;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestAdapter;
import com.github.ljtfreitas.restify.http.client.request.interceptor.HttpClientRequestInterceptor;

@RunWith(MockitoJUnitRunner.class)
public class AsyncHttpClientRequestInterceptorChainTest {

	@Mock
	private AsyncHttpClientRequestInterceptor interceptor;

	@Mock
	private AsyncHttpClientRequest request;

	private AsyncHttpClientRequestInterceptorChain chain;

	@Before
	public void setup() {
		chain = new AsyncHttpClientRequestInterceptorChain(Arrays.asList(interceptor));
	}

	@Test
	public void shouldInterceptRequest() {
		AsyncHttpClientRequest newRequest = mock(AsyncHttpClientRequest.class);

		when(interceptor.interceptsAsync(request))
			.thenReturn(newRequest);

		AsyncHttpClientRequest intercepted = chain.applyAsync(request);

		assertSame(newRequest, intercepted);

		verify(interceptor).interceptsAsync(request);
	}

	@Test
	public void shouldNotModifyRequestWhenInterceptorChainIsEmpty() {
		chain = new AsyncHttpClientRequestInterceptorChain(Collections.emptyList());

		HttpClientRequest intercepted = chain.apply(request);

		assertSame(request, intercepted);
	}

	@Test
	public void shouldAdaptSyncInterceptorToCreateModifyAsyncRequest() {
		HttpClientRequestInterceptor syncInterceptor = mock(HttpClientRequestInterceptor.class);

		HttpClientRequest newRequest = mock(HttpClientRequest.class);

		when(syncInterceptor.intercepts(request))
			.thenReturn(newRequest);

		chain = AsyncHttpClientRequestInterceptorChain.of(Arrays.asList(syncInterceptor));

		AsyncHttpClientRequest intercepted = chain.applyAsync(request);

		assertThat(intercepted, instanceOf(AsyncHttpClientRequestAdapter.class));
	}
}
