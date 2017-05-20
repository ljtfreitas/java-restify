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
package com.github.ljtfreitas.restify.http.client.message.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.RestifyHttpMessageReadException;

public class ByteArrayMessageConverter extends WildcardMessageConverter<byte[]> {

	private static final int DEFAULT_BUFFER_SIZE = 1024;

	private final int bufferSize;

	public ByteArrayMessageConverter() {
		this(DEFAULT_BUFFER_SIZE);
	}

	public ByteArrayMessageConverter(int bufferSize) {
		this.bufferSize= bufferSize;
	}

	@Override
	public boolean canRead(Type type) {
		return byte[].class.equals(type);
	}

	@Override
	public byte[] read(HttpResponseMessage httpResponseMessage, Type expectedType)
			throws RestifyHttpMessageReadException {

		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int len = 0;
			byte[] data = new byte[bufferSize];

			while ((len = httpResponseMessage.body().read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, len);
			}

			buffer.flush();

			return buffer.toByteArray();

		} catch (IOException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}
}
