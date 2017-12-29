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
package com.github.ljtfreitas.restify.http.client.message.converter.octet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.message.io.InputStreamContent;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

public class OctetInputStreamMessageConverter implements OctetStreamMessageConverter<InputStream> {

	private final OctetByteArrayMessageConverter byteArrayMessageConverter;
	private final int bufferSize;

	public OctetInputStreamMessageConverter() {
		this(InputStreamContent.DEFAULT_BUFFER_SIZE);
	}

	public OctetInputStreamMessageConverter(int bufferSize) {
		this.bufferSize = bufferSize;
		this.byteArrayMessageConverter = new OctetByteArrayMessageConverter(bufferSize);
	}

	@Override
	public boolean canRead(Type type) {
		return InputStream.class.equals(type);
	}

	@Override
	public InputStream read(HttpResponseMessage httpResponseMessage, Type expectedType) throws HttpMessageReadException {
		return new ByteArrayInputStream(byteArrayMessageConverter.read(httpResponseMessage, expectedType));
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return InputStream.class.isAssignableFrom(type);
	}

	@Override
	public void write(InputStream body, HttpRequestMessage httpRequestMessage) throws HttpMessageWriteException {
		try {
			InputStreamContent bodyContent = new InputStreamContent(body, bufferSize);
			bodyContent.transferTo(httpRequestMessage.output());

			httpRequestMessage.output().flush();
			httpRequestMessage.output().close();

		} catch (IOException e) {
			throw new HttpMessageWriteException(e);
		}
	}
}
