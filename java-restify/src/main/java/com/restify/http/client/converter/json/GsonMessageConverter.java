package com.restify.http.client.converter.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageWriteException;

public class GsonMessageConverter extends JsonMessageConverter {

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
	public void write(Object body, HttpRequestMessage httpRequestMessage) {
		Charset charset = Charset.forName(httpRequestMessage.charset());

		OutputStreamWriter writer = new OutputStreamWriter(httpRequestMessage.output(), charset);

		try {
			gson.toJson(body, writer);

			writer.close();
		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException("Error on try write json message", e);
		}
	}

	@Override
	public boolean canRead(Class<?> type) {
		return true;
	}

	@Override
	public Object read(Class<?> expectedType, HttpResponseMessage httpResponseMessage) {
		TypeToken<?> token = TypeToken.get(expectedType);

		Reader json = new InputStreamReader(httpResponseMessage.input());

		return this.gson.fromJson(json, token.getType());
	}

}
