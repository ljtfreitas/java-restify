package com.restify.http.client.message;

import com.restify.http.client.request.HttpRequestMessage;
import com.restify.http.client.request.RestifyHttpMessageWriteException;

public interface HttpMessageWriter<T> extends HttpMessageConverter {

	public boolean canWrite(Class<?> type);

	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException;
}
