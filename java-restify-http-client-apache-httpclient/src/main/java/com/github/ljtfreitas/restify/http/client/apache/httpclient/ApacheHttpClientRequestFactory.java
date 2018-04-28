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
package com.github.ljtfreitas.restify.http.client.apache.httpclient;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.Configurable;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.Timeout;

public class ApacheHttpClientRequestFactory implements HttpClientRequestFactory, Closeable {

	private final HttpClient httpClient;
	private final RequestConfig requestConfig;
	private final HttpContext httpContext;
	private final Charset charset;

	public ApacheHttpClientRequestFactory() {
		this(HttpClients.createSystem(), null);
	}

	public ApacheHttpClientRequestFactory(HttpClient httpClient) {
		this(httpClient, null);
	}

	public ApacheHttpClientRequestFactory(RequestConfig requestConfig) {
		this(HttpClients.createSystem(), requestConfig);
	}

	public ApacheHttpClientRequestFactory(HttpContext httpContext) {
		this(HttpClients.createSystem(), null, httpContext);
	}

	public ApacheHttpClientRequestFactory(Charset charset) {
		this(HttpClients.createSystem(), null, null, charset);
	}

	public ApacheHttpClientRequestFactory(HttpClient httpClient, RequestConfig requestConfig) {
		this(httpClient, requestConfig, null);
	}

	public ApacheHttpClientRequestFactory(HttpClient httpClient, RequestConfig requestConfig, HttpContext httpContext) {
		this(httpClient, requestConfig, httpContext, Encoding.UTF_8.charset());
	}

	public ApacheHttpClientRequestFactory(HttpClient httpClient, RequestConfig requestConfig, HttpContext httpContext, Charset charset) {
		this.httpClient = httpClient;
		this.requestConfig = requestConfig;
		this.httpContext = httpContext;
		this.charset = charset;
	}

	@Override
	public ApacheHttpClientRequest createOf(EndpointRequest endpointRequest) {
		HttpUriRequest httpRequest = HttpUriRequestStrategy.of(endpointRequest.method())
				.create(endpointRequest.endpoint().toString());

		HttpContext context = configure(endpointRequest);

		return new ApacheHttpClientRequest(httpClient, httpRequest, context, charset, endpointRequest.headers());
	}

	private HttpContext configure(EndpointRequest source) {
		return Optional.ofNullable(httpContext).orElseGet(() -> buildNewContext(source));
	}

	private HttpContext buildNewContext(EndpointRequest source) {
		HttpContext context = HttpClientContext.create();

		if (context.getAttribute(HttpClientContext.REQUEST_CONFIG) == null) {
			context.setAttribute(HttpClientContext.REQUEST_CONFIG, buildNewConfiguration(source));
		}

		return context;
	}

	private RequestConfig buildNewConfiguration(EndpointRequest source) {
		RequestConfig httpClientConfiguration = (httpClient instanceof Configurable) ? ((Configurable) httpClient).getConfig() : null;

		Builder builder = Optional.ofNullable(requestConfig).map(r -> RequestConfig.copy(r))
				.orElseGet(() -> Optional.ofNullable(httpClientConfiguration)
						.map(r -> RequestConfig.copy(r))
							.orElseGet(RequestConfig::custom));

		builder.setAuthenticationEnabled(true);

		source.metadata().get(Timeout.class).ifPresent(timeout -> {
			builder.setConnectTimeout((int) timeout.connection());
			builder.setConnectionRequestTimeout((int) timeout.connection());
			builder.setSocketTimeout((int) timeout.read());
		});
		
		return builder.build();
	}

	@Override
	public void close() throws IOException {
		if (httpClient instanceof Closeable) {
			((Closeable) this.httpClient).close();
		}
	}
}
