package com.restify.http.client.converter.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;
import com.restify.http.client.converter.HttpMessageConverter;

public abstract class TextMessageConverter implements HttpMessageConverter<String> {

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

	@Override
	public boolean canRead(Class<?> type) {
		return String.class == type;
	}

	@Override
	public String read(Class<? extends String> expectedType, HttpResponseMessage httpResponseMessage) throws RestifyHttpMessageReadException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponseMessage.input()))) {
            return buffer.lines().collect(Collectors.joining("\n"));

		} catch (IOException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}
}
