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
package com.github.ljtfreitas.restify.http.client.response;

import static com.github.ljtfreitas.restify.http.client.Headers.CONTENT_LENGTH;

import java.io.InputStream;

import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;

public abstract class BaseHttpResponseMessage implements HttpResponseMessage {

	private final StatusCode statusCode;
	private final Headers headers;
	private final InputStream body;
	private final HttpRequestMessage httpRequest;

	protected BaseHttpResponseMessage(StatusCode statusCode, Headers headers, InputStream body, HttpRequestMessage httpRequest) {
		this.statusCode = statusCode;
		this.headers = headers;
		this.body = body;
		this.httpRequest = httpRequest;
	}

	@Override
	public HttpRequestMessage request() {
		return httpRequest;
	}

	@Override
	public StatusCode statusCode() {
		return statusCode;
	}
	
	@Override
	public Headers headers() {
		return headers;
	}
	
	@Override
	public InputStream body() {
		return body;
	}
	
	@Override
	public boolean isReadable() {
		return isReadableStatus() && httpMethodShouldContainResponseBody() && hasContentLength();
	}
	
	private boolean httpMethodShouldContainResponseBody() {
		return !(httpRequest.source().method().equals("HEAD") || (httpRequest.source().method().equals("TRACE")));
	}

	private boolean isReadableStatus() {
		return !(statusCode.isInformational() || statusCode.isNoContent() || statusCode.isNotModified());
	}

	private boolean hasContentLength() {
		int contentLength = headers.get(CONTENT_LENGTH).map(h -> Integer.valueOf(h.value())).orElse(-1);
		return contentLength != 0;
	}
}
