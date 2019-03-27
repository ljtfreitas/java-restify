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
package com.github.ljtfreitas.restify.cdi;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerProvider;
import com.github.ljtfreitas.restify.http.client.jdk.HttpClientRequestConfiguration;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.request.interceptor.HttpClientRequestInterceptor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.retry.RetryConfiguration;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractExpressionResolver;
import com.github.ljtfreitas.restify.http.contract.metadata.ContractReader;

public interface RestifyProxyConfiguration {

	public default String endpoint() {
		return null;
	}

	public default HttpClientRequestFactory httpClientRequestFactory() {
		return null;
	}

	public default HttpClientRequestConfiguration httpClientRequestConfiguration() {
		return null;
	}

	public default ContractReader contractReader() {
		return null;
	}

	public default ContractExpressionResolver contractExpressionResolver() {
		return null;
	}

	public default EndpointRequestExecutor endpointRequestExecutor() {
		return null;
	}

	public default Collection<EndpointRequestInterceptor> endpointRequestInterceptors() {
		return Collections.emptyList();
	}

	public default Collection<HttpClientRequestInterceptor> httpClientRequestInterceptors() {
		return Collections.emptyList();
	}

	public default Collection<HttpMessageConverter> converters() {
		return Collections.emptyList();
	}

	public default Collection<EndpointCallHandlerProvider> handlers() {
		return Collections.emptyList();
	}

	public default Authentication authentication() {
		return null;
	}

	public default EndpointResponseErrorFallback endpointResponseErrorFallback() {
		return null;
	}

	public default Executor asyncExecutor() {
		return null;
	}

	public default RetryConfiguration retry() {
		return null;
	}
}
