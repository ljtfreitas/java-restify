package com.restify.http.client.converter;

import java.lang.reflect.Type;

import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;

public interface HttpMessageReader<T> extends HttpMessageConverter {

	public boolean canRead(Type type);

	public T read(HttpResponseMessage httpResponseMessage, Type expectedType) throws RestifyHttpMessageReadException;
}
