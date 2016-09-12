package com.restify.http.client.converter;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.RestifyHttpMessageWriteException;

public interface HttpMessageWriter<T> extends HttpMessageConverter {

	public boolean canWrite(Class<?> type);

	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException;
}
