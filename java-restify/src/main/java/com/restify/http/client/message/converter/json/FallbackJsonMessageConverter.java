package com.restify.http.client.message.converter.json;

import java.lang.reflect.Type;

import com.restify.http.client.request.HttpRequestMessage;
import com.restify.http.client.request.RestifyHttpMessageWriteException;
import com.restify.http.client.response.HttpResponseMessage;
import com.restify.http.client.response.RestifyHttpMessageReadException;

public class FallbackJsonMessageConverter<T> extends JsonMessageConverter<T> {

	@Override
	public boolean canRead(Type type) {
		return false;
	}

	@Override
	public T read(HttpResponseMessage httpResponseMessage, Type expectedType) {
		throw new RestifyHttpMessageReadException("This converter is not able to read your JSON message.");
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return false;
	}

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) {
		throw new RestifyHttpMessageWriteException("This converter is not able to write your JSON message.");
	}
}
