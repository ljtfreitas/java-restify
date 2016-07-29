package com.restify.http.client.converter.form;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.metadata.EndpointMethodQueryStringParameterSerializer;

public abstract class FormURLEncodedMessageConverter<T> implements HttpMessageConverter<T> {

	private final EndpointMethodQueryStringParameterSerializer serializer = new EndpointMethodQueryStringParameterSerializer();

	@Override
	public String contentType() {
		return "application/x-www-form-urlencoded";
	}

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {

		try {
			OutputStreamWriter writer = new OutputStreamWriter(httpRequestMessage.output(), httpRequestMessage.charset());
			writer.write(serializer.serialize(body));

			writer.flush();

		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException(e);
		}
	}

	@Override
	public T read(Class<? extends T> expectedType, HttpResponseMessage httpResponseMessage) throws RestifyHttpMessageReadException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(httpResponseMessage.input()))) {
            String content = buffer.lines().collect(Collectors.joining("\n"));

			ParameterPair[] pairs = Arrays.stream(content.split("&")).map(p -> {
				String[] parameter = p.split("=");
				return new ParameterPair(decode(parameter[0]), decode(parameter[1]));

			}).toArray(s -> new ParameterPair[s]);

            return doRead(pairs);

		} catch (IOException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}

	private String decode(String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	protected abstract T doRead(ParameterPair[] pairs);


}
