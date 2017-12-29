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
package com.github.ljtfreitas.restify.http.client.message.response;

import java.io.InputStream;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;

public abstract class BaseHttpResponseMessage implements HttpResponseMessage {

	public static final String CONTENT_LENGTH = "Content-Length";

	private final StatusCode status;
	private final Headers headers;
	private final InputStream body;
	private final HttpRequestMessage httpRequest;

	protected BaseHttpResponseMessage(StatusCode status, Headers headers, InputStream body,
			HttpRequestMessage httpRequest) {
		this.status = status;
		this.headers = headers;
		this.body = body;
		this.httpRequest = httpRequest;
	}

	@Override
	public HttpRequestMessage request() {
		return httpRequest;
	}

	@Override
	public StatusCode status() {
		return status;
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
	public boolean readable() {
		return readableStatus() && httpMethodShouldContainResponseBody() && hasContentLength();
	}

	private boolean httpMethodShouldContainResponseBody() {
		return !(httpRequest.method().equals("HEAD") || httpRequest.method().equals("TRACE"));
	}

	private boolean readableStatus() {
		return !(status.isInformational() || status.isNoContent() || status.isNotModified());
	}

	private boolean hasContentLength() {
		int contentLength = headers.get(CONTENT_LENGTH).map(Header::value).map(Integer::valueOf).orElse(-1);
		return contentLength != 0;
	}
}
