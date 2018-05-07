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
package com.github.ljtfreitas.restify.http.client.message.converter.json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

public class JsonBMessageConverter<T> implements JsonMessageConverter<T> {

	private final Jsonb jsonb;

	public JsonBMessageConverter() {
		this(JsonbBuilder.create());
	}

	public JsonBMessageConverter(JsonbConfig config) {
		this(JsonbBuilder.create(config));
	}

	public JsonBMessageConverter(Jsonb jsonb) {
		this.jsonb = jsonb;
	}

	@Override
	public boolean canRead(Type type) {
		return true;
	}

	@Override
	public T read(HttpResponseMessage httpResponseMessage, Type expectedType) throws HttpMessageReadException {
		try {
			return jsonb.fromJson(httpResponseMessage.body(), expectedType);
		} catch (JsonbException e) {
			throw new HttpMessageReadException(e);
		}
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return true;
	}

	@Override
	public void write(Object body, HttpRequestMessage httpRequestMessage) throws HttpMessageWriteException {
		OutputStreamWriter writer = new OutputStreamWriter(httpRequestMessage.output(), httpRequestMessage.charset());

		try {
			jsonb.toJson(body, writer);

			writer.close();

		} catch (JsonbException | IOException e) {
			throw new HttpMessageWriteException(e);
		}
	}
}
