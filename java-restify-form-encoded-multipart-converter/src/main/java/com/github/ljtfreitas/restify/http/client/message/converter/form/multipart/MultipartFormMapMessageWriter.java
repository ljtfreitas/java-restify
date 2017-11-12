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

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;

public class MultipartFormMapMessageWriter extends BaseMultipartFormMessageWriter<Map<String, ?>> {

	public MultipartFormMapMessageWriter() {
	}

	protected MultipartFormMapMessageWriter(MultipartFormBoundaryGenerator boundaryGenerator) {
		super(boundaryGenerator);
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return Map.class.isAssignableFrom(type) && supportedMapKey(type);
	}

	private boolean supportedMapKey(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;

			Type mapKeyType = parameterizedType.getActualTypeArguments()[0];

			return (mapKeyType == String.class);

		} else {
			return true;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void doWrite(String boundary, Map<String, ?> body, HttpRequestMessage httpRequestMessage) throws IOException {
		body.forEach((key, value) -> {
			serializers.of(value.getClass())
				.write(boundary, new MultipartField(key, value), httpRequestMessage);
		});
	}
}
