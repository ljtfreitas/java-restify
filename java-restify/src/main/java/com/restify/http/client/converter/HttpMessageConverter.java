package com.restify.http.client.converter;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;

public interface HttpMessageConverter {

	public String contentType();

	public boolean canWrite(Class<?> type);

	public void write(Object body, HttpRequestMessage httpRequestMessage);

	public boolean canRead(Class<?> type);

	public Object read(Class<?> expectedType, HttpResponseMessage httpResponseMessage);
}
