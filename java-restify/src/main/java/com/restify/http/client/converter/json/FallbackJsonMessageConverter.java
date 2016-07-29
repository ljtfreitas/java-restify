package com.restify.http.client.converter.json;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;

public class FallbackJsonMessageConverter<T> extends JsonMessageConverter<T> {

	@Override
	public boolean canWrite(Class<?> type) {
		return false;
	}

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) {
		throw new RestifyHttpMessageWriteException("This converter is not able to write your JSON message.");
	}

	@Override
	public boolean canRead(Class<?> type) {
		return false;
	}

	@Override
	public T read(Class<? extends T> expectedType, HttpResponseMessage httpResponseMessage) {
		throw new RestifyHttpMessageReadException("This converter is not able to read your JSON message.");
	}

}
