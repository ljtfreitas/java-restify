package com.github.ljtfreitas.restify.http.client.message.converter.json;

import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;

import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.RestifyHttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.RestifyHttpMessageReadException;

public class JsonpMessageConverter extends JsonMessageConverter<JsonStructure> {

	@Override
	public boolean canRead(Type type) {
		return type instanceof Class && JsonStructure.class.isAssignableFrom((Class<?>) type);
	}

	@Override
	public JsonStructure read(HttpResponseMessage httpResponseMessage, Type expectedType) throws RestifyHttpMessageReadException {
		if (JsonArray.class.equals(expectedType)) {
			return Json.createReader(httpResponseMessage.body()).readArray();

		} else if (JsonObject.class.equals(expectedType)) {
			return Json.createReader(httpResponseMessage.body()).readObject();

		} else {
			throw new RestifyHttpMessageReadException("Unsupported type: [" + expectedType + "]. Only JsonArray and JsonObject are supported.");
		}
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return JsonStructure.class.isAssignableFrom((Class<?>) type);
	}

	@Override
	public void write(JsonStructure json, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		if (json instanceof JsonArray) {
			Json.createWriter(httpRequestMessage.output()).writeArray((JsonArray) json);

		} else if (json instanceof JsonObject) {
			Json.createWriter(httpRequestMessage.output()).writeObject((JsonObject) json);

		} else {
			throw new RestifyHttpMessageReadException("Unsupported type: [" + json.getClass() + "]. Only JsonArray and JsonObject are supported.");
		}
	}
}
