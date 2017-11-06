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
package com.github.ljtfreitas.restify.http.client.message.converter.form;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.RestifyHttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.RestifyHttpMessageReadException;
import com.github.ljtfreitas.restify.http.contract.ContentType;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodQueryParametersSerializer;

abstract class BaseFormURLEncodedMessageConverter<T> implements FormURLEncodedMessageConverter<T> {

	private static final ContentType APPLICATION_X_WWW_FORM_URLENCODED = ContentType.of("application/x-www-form-urlencoded");

	private final EndpointMethodQueryParametersSerializer serializer = new EndpointMethodQueryParametersSerializer();

	@Override
	public ContentType contentType() {
		return APPLICATION_X_WWW_FORM_URLENCODED;
	}

	@Override
	public T read(HttpResponseMessage httpResponseMessage, Type expectedType) throws RestifyHttpMessageReadException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponseMessage.body()))) {
			String content = buffer.lines().collect(Collectors.joining("\n"));

			ParameterPair[] pairs = Arrays.stream(content.split("&")).map(p -> {
				String[] parameter = p.split("=");
				return new ParameterPair(parameter[0], parameter[1]);
			}).toArray(s -> new ParameterPair[s]);

			return doRead(expectedType, pairs);

		} catch (IOException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}

	protected abstract T doRead(Type expectedType, ParameterPair[] pairs);

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(httpRequestMessage.output(), httpRequestMessage.charset());
			writer.write(serializer.serialize("", String.class, body));

			writer.flush();
			writer.close();

		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException(e);
		}
	}
}
