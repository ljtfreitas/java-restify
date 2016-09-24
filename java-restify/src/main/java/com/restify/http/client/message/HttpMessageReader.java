package com.restify.http.client.message;

import java.lang.reflect.Type;

import com.restify.http.client.response.HttpResponseMessage;
import com.restify.http.client.response.RestifyHttpMessageReadException;

public interface HttpMessageReader<T> extends HttpMessageConverter {

	public boolean canRead(Type type);

	public T read(HttpResponseMessage httpResponseMessage, Type expectedType) throws RestifyHttpMessageReadException;
}
