package com.restify.http.client.message.converter.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

import com.restify.http.client.message.HttpMessageReader;
import com.restify.http.client.message.HttpMessageWriter;
import com.restify.http.client.request.HttpRequestMessage;
import com.restify.http.client.request.RestifyHttpMessageWriteException;
import com.restify.http.client.response.HttpResponseMessage;
import com.restify.http.client.response.RestifyHttpMessageReadException;

public abstract class TextMessageConverter implements HttpMessageReader<String>, HttpMessageWriter<String> {

	@Override
	public boolean canRead(Type type) {
		return String.class == type;
	}

	@Override
	public String read(HttpResponseMessage httpResponseMessage, Type expectedType) throws RestifyHttpMessageReadException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponseMessage.body()))) {
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
