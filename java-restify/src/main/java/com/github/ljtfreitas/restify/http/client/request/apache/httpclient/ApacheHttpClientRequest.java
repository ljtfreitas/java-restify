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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;

import com.github.ljtfreitas.restify.http.RestifyHttpException;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;

public class ApacheHttpClientRequest implements HttpClientRequest {

	private final HttpClient httpClient;
	private final HttpUriRequest httpRequest;
	private final HttpContext httpContext;
	private final Charset charset;
	private final Headers headers;
	private final EndpointRequest source;

	private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024 * 100);
	private final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

	public ApacheHttpClientRequest(HttpClient httpClient, HttpUriRequest httpRequest, HttpContext httpContext, Charset charset,
			Headers headers, EndpointRequest source) {
		this.httpClient = httpClient;
		this.httpRequest = httpRequest;
		this.httpContext = httpContext;
		this.charset = charset;
		this.headers = headers;
		this.source = source;
	}

	@Override
	public HttpResponseMessage execute() throws RestifyHttpException {
		headers.all().forEach(h -> httpRequest.addHeader(h.name(), h.value()));

		if (httpRequest instanceof HttpEntityEnclosingRequest) {
			HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
			HttpEntity requestEntity = new ByteArrayEntity(byteArrayOutputStream.toByteArray());
			entityEnclosingRequest.setEntity(requestEntity);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpRequest, httpContext);

			return responseOf(httpResponse);

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}

	}

	private ApacheHttpClientResponse responseOf(HttpResponse httpResponse) throws IOException {
		StatusCode statusCode = StatusCode.of(httpResponse.getStatusLine().getStatusCode());

		Headers headers = new Headers();
		Arrays.stream(httpResponse.getAllHeaders())
			.forEach(h -> headers.add(new com.github.ljtfreitas.restify.http.client.Header(h.getName(), h.getValue())));

		HttpEntity entity = httpResponse.getEntity();

		InputStream stream = entity != null ? entity.getContent()
				: new ByteArrayInputStream(new byte[0]);

		return new ApacheHttpClientResponse(statusCode, headers, stream, entity, httpResponse, this);
	}

	@Override
	public OutputStream output() {
		return bufferedOutputStream;
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public Headers headers() {
		return headers;
	}

	@Override
	public EndpointRequest source() {
		return source;
	}
}
