package com.restify.http.client.converter.form.multipart;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.metadata.MultipartParameters;

public class MultipartFormParametersMessageConverter extends MultipartFormMessageConverter<MultipartParameters> {

	private final MultipartFormMapMessageConverter mapMessageConverter = new MultipartFormMapMessageConverter();

	public MultipartFormParametersMessageConverter() {
	}

	protected MultipartFormParametersMessageConverter(MultipartFormBoundaryGenerator boundaryGenerator) {
		super(boundaryGenerator);
	}

	@Override
	public boolean writerOf(Class<?> type) {
		return type == MultipartParameters.class;
	}

	@Override
	protected void doWrite(String boundary, MultipartParameters body, HttpRequestMessage httpRequestMessage)
			throws IOException {

		Map<String, Object> bodyAsMap = new LinkedHashMap<>();

		body.all().forEach(part -> bodyAsMap.put(part.name(), part.values()));

		mapMessageConverter.doWrite(boundary, bodyAsMap, httpRequestMessage);
	}
}
