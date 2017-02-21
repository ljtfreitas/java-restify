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
package com.github.ljtfreitas.restify.http.spring.client.request;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.response.BaseHttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.DefaultEndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseErrorFallback;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;
import com.github.ljtfreitas.restify.http.util.Tryable;

public class EndpointResponseErrorHandler extends DefaultResponseErrorHandler {

	private final EndpointResponseErrorFallback endpointResponseErrorFallback;

	public EndpointResponseErrorHandler() {
		this.endpointResponseErrorFallback = new DefaultEndpointResponseErrorFallback();
	}

	public EndpointResponseErrorHandler(EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.endpointResponseErrorFallback = endpointResponseErrorFallback;
	}

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		HttpResponseMessage httpResponse = convert(response);
		endpointResponseErrorFallback.onError(httpResponse);
	}

	private HttpResponseMessage convert(ClientHttpResponse response) throws IOException {
		StatusCode statusCode = StatusCode.of(response.getRawStatusCode());
		Headers headers = headersOf(response.getHeaders());
		InputStream body = Tryable.or(() -> response.getBody(), new ByteArrayInputStream(new byte[0]));

		return new BaseHttpResponseMessage(statusCode, headers, body, null) {
			@Override
			public void close() throws IOException {
				response.close();
			}
		};
	}

	private Headers headersOf(HttpHeaders httpHeaders) {
		Headers headers = new Headers();
		httpHeaders.forEach((k, v) -> headers.put(k, v));
		return headers;
	}
}
