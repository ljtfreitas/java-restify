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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.message.io.InputStreamContent;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

public class OctetByteArrayMessageConverter implements OctetStreamMessageConverter<byte[]> {

	private final int bufferSize;

	public OctetByteArrayMessageConverter() {
		this(InputStreamContent.DEFAULT_BUFFER_SIZE);
	}

	public OctetByteArrayMessageConverter(int bufferSize) {
		this.bufferSize = bufferSize;
	}


	@Override
	public boolean canRead(Type type) {
		return byte[].class.equals(type);
	}

	@Override
	public byte[] read(HttpResponseMessage httpResponseMessage, Type expectedType)
			throws HttpMessageReadException {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			InputStreamContent bodyContent = new InputStreamContent(httpResponseMessage.body(), bufferSize);
			bodyContent.transferTo(buffer);

			buffer.flush();

			return buffer.toByteArray();

		} catch (IOException e) {
			throw new HttpMessageReadException(e);
		}
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return byte[].class.equals(type);
	}

	@Override
	public void write(byte[] body, HttpRequestMessage httpRequestMessage) throws HttpMessageWriteException {
		try {
			OutputStream output = httpRequestMessage.body();

			output.write(body);
			output.flush();
			output.close();

		} catch (IOException e) {
			throw new HttpMessageWriteException(e);
		}
	}
}
