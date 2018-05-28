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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReaderFactory;
import javax.json.JsonStructure;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.util.Tryable;

public class JsonpMessageConverter implements JsonMessageConverter<JsonStructure> {

	private final JsonReaderFactory jsonReaderFactory;
	private final JsonWriterFactory jsonWriterFactory;

	public JsonpMessageConverter() {
		this(Collections.emptyMap());
	}

	public JsonpMessageConverter(Map<String, ?> configuration) {
		this.jsonReaderFactory = Json.createReaderFactory(configuration);
		this.jsonWriterFactory = Json.createWriterFactory(configuration);
	}

	@Override
	public boolean canRead(Type type) {
		return type instanceof Class && JsonStructure.class.isAssignableFrom((Class<?>) type);
	}

	@Override
	public JsonStructure read(HttpResponseMessage httpResponseMessage, Type expectedType) throws HttpMessageReadException {
		if (JsonArray.class.equals(expectedType)) {
			return jsonReaderFactory.createReader(httpResponseMessage.body()).readArray();

		} else if (JsonObject.class.equals(expectedType)) {
			return jsonReaderFactory.createReader(httpResponseMessage.body()).readObject();

		} else {
			throw new HttpMessageReadException("Unsupported type: [" + expectedType + "]. Only JsonArray and JsonObject are supported.");
		}
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return JsonStructure.class.isAssignableFrom((Class<?>) type);
	}

	@Override
	public void write(JsonStructure json, HttpRequestMessage httpRequestMessage) throws HttpMessageWriteException {
		if (json instanceof JsonArray) {
			jsonWriterTo(httpRequestMessage, writer -> writer.writeArray((JsonArray) json));

		} else if (json instanceof JsonObject) {
			jsonWriterTo(httpRequestMessage, writer -> writer.writeObject((JsonObject) json));

		} else {
			throw new HttpMessageReadException("Unsupported type: [" + json.getClass() + "]. Only JsonArray and JsonObject are supported.");
		}
	}

	private void jsonWriterTo(HttpRequestMessage httpRequestMessage, Consumer<JsonWriter> consumer) {
		JsonWriter writer = jsonWriterFactory.createWriter(httpRequestMessage.body());

		consumer.accept(writer);

		Tryable.run(() -> {;
			httpRequestMessage.body().flush();
			httpRequestMessage.body().close();

		}, e -> new HttpMessageWriteException(e));
	}
}
