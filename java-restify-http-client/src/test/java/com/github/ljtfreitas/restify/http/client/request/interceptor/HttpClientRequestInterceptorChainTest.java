package com.github.ljtfreitas.restify.http.client.request.interceptor;

import static org.junit.Assert.*;
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

@RunWith(MockitoJUnitRunner.class)
public class HttpClientRequestInterceptorChainTest {

	@Mock
	private HttpClientResponseInterceptor interceptor;

	@Mock
	private HttpClientRequest request;

	private HttpClientRequestInterceptorChain chain;

	@Before
	public void setup() {
		chain = new HttpClientRequestInterceptorChain(Arrays.asList(interceptor));
	}

	@Test
	public void shouldInterceptRequest() {
		HttpClientRequest newRequest = mock(HttpClientRequest.class);

		when(interceptor.intercepts(request))
			.thenReturn(newRequest);

		HttpClientRequest intercepted = chain.apply(request);

		assertSame(newRequest, intercepted);

		verify(interceptor).intercepts(request);
	}

	@Test
	public void shouldNotModifyRequestWhenInterceptorChainIsEmpty() {
		chain = new HttpClientRequestInterceptorChain(Collections.emptyList());

		HttpClientRequest intercepted = chain.apply(request);

		assertSame(request, intercepted);
	}
}
