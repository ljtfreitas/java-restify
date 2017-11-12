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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class GsonMessageConverter<T> implements JsonMessageConverter<T> {

	private final Gson gson;

	public GsonMessageConverter() {
		this(new Gson());
	}

	public GsonMessageConverter(Gson gson) {
		this.gson = new Gson();
	}

	@Override
	public boolean canRead(Type type) {
		return true;
	}

	@Override
	public T read(HttpResponseMessage httpResponseMessage, Type expectedType) throws HttpMessageReadException {
		try {
			TypeToken<?> token = TypeToken.get(expectedType);

			Reader json = new InputStreamReader(httpResponseMessage.body());

			return this.gson.fromJson(json, token.getType());
		} catch (JsonIOException | JsonSyntaxException e) {
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
			gson.toJson(body, writer);

			writer.close();

		} catch (JsonIOException | IOException e) {
			throw new HttpMessageWriteException(e);
		}
	}
}
