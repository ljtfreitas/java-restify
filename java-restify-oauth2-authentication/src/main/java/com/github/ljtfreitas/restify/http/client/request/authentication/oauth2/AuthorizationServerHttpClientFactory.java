/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.github.ljtfreitas.restify.http.client.jdk.HttpClientRequestConfiguration;
import com.github.ljtfreitas.restify.http.client.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedParametersMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.XmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.DefaultEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncEndpointRequestExecutorAdapter;
import com.github.ljtfreitas.restify.http.client.request.async.AsyncHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.async.DefaultAsyncEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.spi.Provider;
import com.github.ljtfreitas.restify.util.async.DisposableExecutors;

public class AuthorizationServerHttpClientFactory {

	private final HttpMessageConverters converters;
	private final HttpClientRequestFactory httpClientRequestFactory;

	public AuthorizationServerHttpClientFactory() {
		this.httpClientRequestFactory = httpClientRequestFactory();
		this.converters = converters();
	}

	public AuthorizationServerHttpClientFactory(HttpClientRequestFactory httpClientRequestFactory) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		this.converters = converters();
	}

	public AuthorizationServerHttpClientFactory(HttpMessageConverters converters) {
		this.converters = converters;
		this.httpClientRequestFactory = httpClientRequestFactory();
	}

	public EndpointRequestExecutor create() {
		return doCreate();
	}

	private EndpointRequestExecutor doCreate() {
		return new DefaultEndpointRequestExecutor(httpClientRequestFactory, endpointRequestWriter(converters), endpointResponseReader(converters));
	}

	public AsyncEndpointRequestExecutor createAsync() {
		ExecutorService threadPool = DisposableExecutors.newCachedThreadPool();
		return httpClientRequestFactory instanceof AsyncHttpClientRequestFactory ?
			new DefaultAsyncEndpointRequestExecutor(threadPool, (AsyncHttpClientRequestFactory) httpClientRequestFactory,
					endpointRequestWriter(converters), endpointResponseReader(converters)) :
				new AsyncEndpointRequestExecutorAdapter(threadPool, doCreate());
	}

	private HttpClientRequestFactory httpClientRequestFactory() {
		HttpClientRequestConfiguration httpClientRequestConfiguration = new HttpClientRequestConfiguration.Builder()
				.followRedirects()
					.disabled()
				.useCaches()
					.disabled()
				.build();

		return new JdkHttpClientRequestFactory(httpClientRequestConfiguration);
	}

	private EndpointRequestWriter endpointRequestWriter(HttpMessageConverters converters) {
		return new EndpointRequestWriter(converters);
	}

	private EndpointResponseReader endpointResponseReader(HttpMessageConverters converters) {
		return new EndpointResponseReader(converters, new AuthorizationServerResponseErrorFallback());
	}

	private HttpMessageConverters converters() {
		Collection<HttpMessageConverter> converters = new ArrayList<>();

		converters.add(new TextPlainMessageConverter());
		converters.add(new FormURLEncodedParametersMessageConverter());

		Provider provider = new Provider();
		provider.single(JsonMessageConverter.class).ifPresent(converters::add);
		provider.single(XmlMessageConverter.class).ifPresent(converters::add);

		return new HttpMessageConverters(converters);
	}
}