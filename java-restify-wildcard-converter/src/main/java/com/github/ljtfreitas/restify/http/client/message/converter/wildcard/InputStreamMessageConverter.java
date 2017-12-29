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
package com.github.ljtfreitas.restify.http.client.message.converter.wildcard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

public class InputStreamMessageConverter implements WildcardMessageConverter<InputStream> {

	private final ByteArrayMessageConverter byteArrayMessageConverter;

	public InputStreamMessageConverter() {
		this.byteArrayMessageConverter = new ByteArrayMessageConverter();
	}

	public InputStreamMessageConverter(int bufferSize) {
		this.byteArrayMessageConverter = new ByteArrayMessageConverter(bufferSize);
	}

	@Override
	public boolean canRead(Type type) {
		return InputStream.class.equals(type);
	}

	@Override
	public InputStream read(HttpResponseMessage httpResponseMessage, Type expectedType)
			throws HttpMessageReadException {

		return new ByteArrayInputStream(byteArrayMessageConverter.read(httpResponseMessage, expectedType));
	}
}
