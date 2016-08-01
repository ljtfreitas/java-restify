package com.restify.http.client.converter;

import java.lang.reflect.Type;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;

public interface HttpMessageConverter<T> {

	public String contentType();

	public boolean canRead(Type type);

	public T read(Type expectedType, HttpResponseMessage httpResponseMessage) throws RestifyHttpMessageReadException;

	public boolean canWrite(Class<?> type);

	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException;
}
