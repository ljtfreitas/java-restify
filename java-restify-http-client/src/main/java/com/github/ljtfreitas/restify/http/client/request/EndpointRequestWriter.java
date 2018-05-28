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
package com.github.ljtfreitas.restify.http.client.request;

import static com.github.ljtfreitas.restify.http.client.message.Headers.CONTENT_TYPE;

import java.io.IOException;

import com.github.ljtfreitas.restify.http.client.message.ContentType;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriter;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;

public class EndpointRequestWriter {

	private final HttpMessageConverters messageConverters;

	public EndpointRequestWriter(HttpMessageConverters messageConverters) {
		this.messageConverters = messageConverters;
	}

	public void write(EndpointRequest endpointRequest, HttpRequestMessage httpRequestMessage) {
		Object body = endpointRequest.body()
				.orElseThrow(() -> new IllegalArgumentException("Your request does not have a body."));

		doWrite(body, httpRequestMessage);
	}

	private void doWrite(Object body, HttpRequestMessage httpRequestMessage) {
		ContentType contentType = contentTypeOf(httpRequestMessage);

		HttpMessageWriter<Object> writer = writerOf(contentType, body);

		try {
			writer.write(body, httpRequestMessage);

			httpRequestMessage.body().flush();
			httpRequestMessage.body().close();

		} catch (IOException e) {
			throw new HttpMessageWriteException("Error on write HTTP body of type [" + contentType + "].", e);
		}
	}

	private ContentType contentTypeOf(HttpRequestMessage httpRequestMessage) {
		String contentTypeHeader = httpRequestMessage.headers().get(CONTENT_TYPE)
			.map(Header::value)
				.orElseThrow(() -> new HttpMessageWriteException("Your request has a body, but the header [Content-Type] "
					+ "it was not provided."));

		return ContentType.of(contentTypeHeader);
	}

	private HttpMessageWriter<Object> writerOf(ContentType contentType, Object body) {
		return messageConverters.writerOf(contentType, body.getClass())
				.orElseThrow(() -> new HttpMessageWriteException("Your request has a [Content-Type] "
					+ "of type [" + contentType.name() + "], but there is no MessageConverter able to write your message."));
	}
}
