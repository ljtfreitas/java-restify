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
package com.github.ljtfreitas.restify.http.client.request.apache.httpclient;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

import com.github.ljtfreitas.restify.http.client.charset.Encoding;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;

public class ApacheHttpClientRequestFactory implements HttpClientRequestFactory {

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
		this(HttpClients.createSystem(), null, HttpClientContext.create(), charset);
	}

	public ApacheHttpClientRequestFactory(HttpClient httpClient, RequestConfig requestConfig) {
		this(httpClient, requestConfig, HttpClientContext.create());
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

		HttpContext context = configure(httpRequest);

		return new ApacheHttpClientRequest(httpClient, httpRequest, context, charset, endpointRequest.headers(), endpointRequest);
	}

	private HttpContext configure(HttpUriRequest httpRequest) {
		if (httpContext.getAttribute(HttpClientContext.REQUEST_CONFIG) == null) {
			RequestConfig configuration = Optional.ofNullable(requestConfig)
					.orElseGet(() -> buildNewConfiguration());

			httpContext.setAttribute(HttpClientContext.REQUEST_CONFIG, configuration);
		}

		return httpContext;
	}

	private RequestConfig buildNewConfiguration() {
		 return RequestConfig.custom()
			.setAuthenticationEnabled(true)
				.build();
	}

	private enum HttpUriRequestStrategy {

		GET {
			HttpUriRequest create(String endpoint) {
				return new HttpGet(endpoint);
			}
		},
		HEAD {
			HttpUriRequest create(String endpoint) {
				return new HttpHead(endpoint);
			}
		},
		POST {
			HttpUriRequest create(String endpoint) {
				return new HttpPost(endpoint);
			}
		},
		PUT {
			HttpUriRequest create(String endpoint) {
				return new HttpPut(endpoint);
			}
		},
		PATCH {
			HttpUriRequest create(String endpoint) {
				return new HttpPatch(endpoint);
			}
		},
		DELETE {
			HttpUriRequest create(String endpoint) {
				return new HttpDelete(endpoint);
			}
		},
		OPTIONS {
			HttpUriRequest create(String endpoint) {
				return new HttpOptions(endpoint);
			}
		},
		TRACE {
			HttpUriRequest create(String endpoint) {
				return new HttpTrace(endpoint);
			}
		};

		abstract HttpUriRequest create(String endpoint);

		static HttpUriRequestStrategy of(String method) {
			return Arrays.stream(HttpUriRequestStrategy.values())
					.filter(m -> m.name().equals(method))
						.findFirst()
							.orElseThrow(() -> new IllegalArgumentException("Unsupported http method: " + method));
		}
	}
}
