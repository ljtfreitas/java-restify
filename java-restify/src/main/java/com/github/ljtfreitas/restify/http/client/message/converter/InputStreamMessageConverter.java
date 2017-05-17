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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.client.message.HttpMessageReader;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.RestifyHttpMessageReadException;

public class InputStreamMessageConverter implements HttpMessageReader<InputStream> {

	private static final String WILDCARD_CONTENT_TYPE = "*/*";

	@Override
	public String contentType() {
		return WILDCARD_CONTENT_TYPE;
	}

	@Override
	public boolean canRead(Type type) {
		return InputStream.class.equals(type);
	}

	@Override
	public InputStream read(HttpResponseMessage httpResponseMessage, Type expectedType)
			throws RestifyHttpMessageReadException {

		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int len = 0;
			byte[] data = new byte[1024];

			while ((len = httpResponseMessage.body().read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, len);
			}

			buffer.flush();

			return new ByteArrayInputStream(buffer.toByteArray());

		} catch (IOException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}
}
