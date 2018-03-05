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

import java.util.Optional;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.Configurable;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.Timeout;

class HttpContextBuilder {

	private final HttpContext httpContext;
	private final RequestConfig requestConfig;
	private final Configurable configurable;

	HttpContextBuilder(HttpContext httpContext, RequestConfig requestConfig, Object configurable) {
		this.httpContext = httpContext;
		this.requestConfig = requestConfig;
		this.configurable = configurable instanceof Configurable ? (Configurable) configurable : null;
	}

	HttpContext buildTo(EndpointRequest source) {
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
		RequestConfig httpClientConfiguration = Optional.ofNullable(configurable)
				.map(Configurable::getConfig)
					.orElse(null);

		Builder builder = Optional.ofNullable(requestConfig)
				.map(r -> RequestConfig.copy(r))
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
}
