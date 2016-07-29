package com.restify.http.client.converter;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;

public interface HttpMessageConverter<T> {

	public String contentType();

	public boolean canWrite(Class<?> type);

	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException;

	public boolean canRead(Class<?> type);

	public T read(Class<? extends T> expectedType, HttpResponseMessage httpResponseMessage) throws RestifyHttpMessageReadException;
}
