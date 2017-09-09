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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.RestifyHttpException;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.util.Tryable;
import com.netflix.client.ClientException;

public class RibbonHttpClientRequest implements HttpClientRequest {

	private final EndpointRequest endpointRequest;
	private final RibbonLoadBalancedClient ribbonLoadBalancedClient;
	private final Charset charset;

	private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024 * 100);
	private final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

	public RibbonHttpClientRequest(EndpointRequest endpointRequest, RibbonLoadBalancedClient ribbonLoadBalancedClient, Charset charset) {
		this.endpointRequest = endpointRequest;
		this.ribbonLoadBalancedClient = ribbonLoadBalancedClient;
		this.charset = charset;
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
	public OutputStream output() {
		return bufferedOutputStream;
	}

	@Override
	public Headers headers() {
		return endpointRequest.headers();
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public HttpResponseMessage execute() throws RestifyHttpException {
		try {
			RibbonResponse response = ribbonLoadBalancedClient.executeWithLoadBalancer(new RibbonRequest(this));

			return response.unwrap();

		} catch (ClientException e) {
			throw new RestifyHttpException("Error on HTTP request: [" + endpointRequest.method() + " " +
					endpointRequest.endpoint() + "]", e);
		}
	}

	public URI ribbonEndpoint() {
		String sourceEndpoint = endpointRequest.endpoint().toString();
		return URI.create(sourceEndpoint.replaceFirst(endpointRequest.endpoint().getHost(), ""));
	}

	public EndpointRequest replace(URI ribbonEndpoint) {
		return endpointRequest.replace(ribbonEndpoint);
	}

	public boolean isGet() {
		return endpointRequest.method().equalsIgnoreCase("GET");
	}

	public void writeTo(HttpClientRequest httpRequestMessage) {
		endpointRequest.body()
			.ifPresent(b -> Tryable.run(() -> byteArrayOutputStream.writeTo(httpRequestMessage.output())));
	}

	public String serviceName() {
		return endpointRequest.endpoint().getHost();
	}
}
