package com.restify.http.client.converter.json;

import java.lang.reflect.Type;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;

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
