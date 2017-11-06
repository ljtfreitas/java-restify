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
package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import static com.github.ljtfreitas.restify.http.client.header.Headers.CONTENT_TYPE;

import java.io.IOException;
import java.io.OutputStream;

import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.RestifyHttpMessageWriteException;
import com.github.ljtfreitas.restify.http.contract.ContentType;

abstract class BaseMultipartFormMessageWriter<T> implements MultipartFormMessageWriter<T> {

	private static final String MULTIPART_FORM_DATA = "multipart/form-data";

	protected final MultipartFieldSerializers serializers = new MultipartFieldSerializers();

	private final MultipartFormBoundaryGenerator boundaryGenerator;

	public BaseMultipartFormMessageWriter() {
		this.boundaryGenerator = new UUIDMultipartFormBoundaryGenerator();
	}

	protected BaseMultipartFormMessageWriter(MultipartFormBoundaryGenerator boundaryGenerator) {
		this.boundaryGenerator = boundaryGenerator;
	}

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		try {
			String boundary = boundaryGenerator.generate();

			httpRequestMessage = addToContentType(boundary, httpRequestMessage);

			doWrite("------" + boundary, body, httpRequestMessage);

			OutputStream output = httpRequestMessage.output();
			output.write('\r');
			output.write('\n');
			output.write(("------" + boundary + "--").getBytes());

			output.flush();
			output.close();

		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException(e);
		}
	}

	private HttpRequestMessage addToContentType(String boundary, HttpRequestMessage httpRequestMessage) {
		Header contentTypeHeader = httpRequestMessage.headers().get(CONTENT_TYPE)
				.orElseGet(() -> Header.contentType(MULTIPART_FORM_DATA));

		ContentType contentType = ContentType.of(contentTypeHeader.value());

		return httpRequestMessage.replace(Header.contentType(contentType.append("boundary", "----" + boundary)));
	}

	protected abstract void doWrite(String boundary, T body, HttpRequestMessage httpRequestMessage) throws IOException;
}
