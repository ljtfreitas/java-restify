package com.restify.http.client.converter.form;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;
import com.restify.http.client.converter.HttpMessageReader;
import com.restify.http.client.converter.HttpMessageWriter;
import com.restify.http.metadata.EndpointMethodQueryParametersSerializer;

public abstract class FormURLEncodedMessageConverter<T> implements HttpMessageReader<T>, HttpMessageWriter<T> {

	private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

	private final EndpointMethodQueryParametersSerializer serializer = new EndpointMethodQueryParametersSerializer();

	@Override
	public String contentType() {
		return APPLICATION_X_WWW_FORM_URLENCODED;
	}

	@Override
	public T read(Type expectedType, HttpResponseMessage httpResponseMessage) throws RestifyHttpMessageReadException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponseMessage.body()))) {
			String content = buffer.lines().collect(Collectors.joining("\n"));

			ParameterPair[] pairs = Arrays.stream(content.split("&")).map(p -> {
				String[] parameter = p.split("=");
				return new ParameterPair(parameter[0], parameter[1]);
			}).toArray(s -> new ParameterPair[s]);

			return doRead(expectedType, pairs);

		} catch (IOException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}

	protected abstract T doRead(Type expectedType, ParameterPair[] pairs);

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(httpRequestMessage.output(), httpRequestMessage.charset());
			writer.write(serializer.serialize("", String.class, body));

			writer.flush();
			writer.close();

		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException(e);
		}
	}
}
