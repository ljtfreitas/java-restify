package com.restify.http.client.converter.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;

public class GsonMessageConverter<T> extends JsonMessageConverter<T> {

	private final Gson gson;

	public GsonMessageConverter() {
		this(new Gson());
	}

	public GsonMessageConverter(Gson gson) {
		this.gson = new Gson();
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return true;
	}

	@Override
	public void write(Object body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		Charset charset = Charset.forName(httpRequestMessage.charset());

		OutputStreamWriter writer = new OutputStreamWriter(httpRequestMessage.output(), charset);

		try {
			gson.toJson(body, writer);

			writer.close();

		} catch (JsonIOException | IOException e) {
			throw new RestifyHttpMessageWriteException(e);
		}
	}

	@Override
	public boolean canRead(Type type) {
		return true;
	}

	@Override
	public T read(Type expectedType, HttpResponseMessage httpResponseMessage) throws RestifyHttpMessageReadException {
		try {
			TypeToken<?> token = TypeToken.get(expectedType);

			Reader json = new InputStreamReader(httpResponseMessage.input());

			return this.gson.fromJson(json, token.getType());
		} catch (JsonIOException | JsonSyntaxException e) {
			throw new RestifyHttpMessageReadException(e);
		}
	}

}
