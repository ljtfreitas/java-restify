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
package com.github.ljtfreitas.restify.http.netflix.client.request;

import java.net.URI;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.HttpException;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;
import com.github.ljtfreitas.restify.util.Tryable;
import com.netflix.client.ClientException;

public class DefaultRibbonHttpClientRequest extends BaseRibbonHttpClientRequest implements HttpClientRequest {

	private final EndpointRequest endpointRequest;
	private final RibbonLoadBalancedClient ribbonLoadBalancedClient;
	private final Charset charset;
	private final HttpRequestBody body;

	public DefaultRibbonHttpClientRequest(EndpointRequest endpointRequest, RibbonLoadBalancedClient ribbonLoadBalancedClient, Charset charset) {
		this(endpointRequest, ribbonLoadBalancedClient, charset, new BufferedHttpRequestBody(charset));
	}

	private DefaultRibbonHttpClientRequest(EndpointRequest endpointRequest, RibbonLoadBalancedClient ribbonLoadBalancedClient, Charset charset,
			HttpRequestBody body) {
		super(endpointRequest);
		this.endpointRequest = endpointRequest;
		this.ribbonLoadBalancedClient = ribbonLoadBalancedClient;
		this.charset = charset;
		this.body = body;
	}

	@Override
	public URI uri() {
		return endpointRequest.endpoint();
	}

	@Override
	public String method() {
		return endpointRequest.method();
	}

	@Override
	public HttpRequestBody body() {
		return body;
	}

	@Override
	public Headers headers() {
		return endpointRequest.headers();
	}

	@Override
	public HttpRequestMessage replace(Header header) {
		return new DefaultRibbonHttpClientRequest(endpointRequest.replace(header), ribbonLoadBalancedClient, charset, body);
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public HttpClientResponse execute() throws HttpException {
		try {
			RibbonResponse response = ribbonLoadBalancedClient.withLoadBalancer(new RibbonRequest(this));

			return response.unwrap();

		} catch (ClientException e) {
			throw new HttpClientException("Error on HTTP request: [" + endpointRequest.method() + " " +
					endpointRequest.endpoint() + "]", e);
		}
	}

	@Override
	public void writeTo(HttpClientRequest httpRequestMessage) {
		endpointRequest.body()
			.ifPresent(b ->
				Tryable.run(() -> {
					body.writeTo(httpRequestMessage.body().output());
					httpRequestMessage.body().output().flush();
					httpRequestMessage.body().output().close();
				})
			);
	}

	@Override
	public RibbonHttpClientRequest replace(URI ribbonEndpoint) {
		return new DefaultRibbonHttpClientRequest(endpointRequest.replace(ribbonEndpoint), ribbonLoadBalancedClient, charset,
				body);
	}
}
