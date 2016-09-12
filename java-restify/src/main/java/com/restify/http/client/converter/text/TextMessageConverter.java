package com.restify.http.client.converter.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;
import com.restify.http.client.converter.HttpMessageReader;
import com.restify.http.client.converter.HttpMessageWriter;

public abstract class TextMessageConverter implements HttpMessageReader<String>, HttpMessageWriter<String> {

	@Override
	public boolean canRead(Type type) {
		return String.class == type;
	}

	@Override
	public String read(Type expectedType, HttpResponseMessage httpResponseMessage) throws RestifyHttpMessageReadException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponseMessage.input()))) {
            return buffer.lines().collect(Collectors.joining("\n"));

		} catch (IOException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return String.class == type;
	}

	@Override
	public void write(String body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		try {
			httpRequestMessage.output().write(body.toString().getBytes());
		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException(e);
		}
	}
}
