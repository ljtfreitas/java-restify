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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;

class ApacheHttpResponseReader {

	private final HttpResponse httpResponse;
	private final HttpRequestMessage source;

	ApacheHttpResponseReader(HttpResponse httpResponse, HttpRequestMessage source) {
		this.httpResponse = httpResponse;
		this.source = source;
	}

	ApacheHttpClientResponse read() throws IOException {
		StatusLine statusLine = httpResponse.getStatusLine();

		StatusCode statusCode = StatusCode.of(statusLine.getStatusCode(), statusLine.getReasonPhrase());

		Headers headers = Arrays.stream(httpResponse.getAllHeaders())
			.reduce(new Headers(), (a, b) -> a.add(b.getName(), b.getValue()), (a, b) -> b);

		HttpEntity entity = httpResponse.getEntity();

		InputStream stream = entity != null ? entity.getContent() : new ByteArrayInputStream(new byte[0]);

		return new ApacheHttpClientResponse(statusCode, headers, stream, entity, httpResponse, source);
	}
}
