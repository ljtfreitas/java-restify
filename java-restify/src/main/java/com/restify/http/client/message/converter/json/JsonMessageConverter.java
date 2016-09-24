package com.restify.http.client.message.converter.json;

import com.restify.http.client.message.HttpMessageReader;
import com.restify.http.client.message.HttpMessageWriter;
import com.restify.http.contract.metadata.reflection.JavaClassDiscovery;

public abstract class JsonMessageConverter<T> implements HttpMessageReader<T>, HttpMessageWriter<T> {

	private static final String APPLICATION_JSON = "application/json";

	@Override
	public String contentType() {
		return APPLICATION_JSON;
	}

	public static JsonMessageConverter<Object> available() {
		return JavaClassDiscovery.present("com.fasterxml.jackson.databind.ObjectMapper") ? new JacksonMessageConverter<>()
			: JavaClassDiscovery.present("com.google.gson.Gson") ? new GsonMessageConverter<>()
				:  new FallbackJsonMessageConverter<>();
	}
}
