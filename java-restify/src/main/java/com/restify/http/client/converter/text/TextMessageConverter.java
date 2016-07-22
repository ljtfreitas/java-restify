package com.restify.http.client.converter.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.converter.HttpMessageConverter;

public abstract class TextMessageConverter implements HttpMessageConverter {

	@Override
	public boolean canWrite(Class<?> type) {
		return String.class == type;
	}

	@Override
	public void write(Object body, HttpRequestMessage httpRequestMessage) {
		try {
			httpRequestMessage.output().write(body.toString().getBytes());
		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

	@Override
	public boolean canRead(Class<?> type) {
		return String.class == type;
	}

	@Override
	public Object read(Class<?> expectedType, HttpResponseMessage httpResponseMessage) {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponseMessage.input()))) {
            return buffer.lines().collect(Collectors.joining("\n"));

		} catch (IOException e) {
        	throw new RestifyHttpException(e);
		}
	}


}
