package com.restify.http.client.message.form.multipart;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.restify.http.client.request.HttpRequestMessage;
import com.restify.http.contract.MultipartParameters;

public class MultipartFormParametersMessageWriter extends MultipartFormMessageWriter<MultipartParameters> {

	private final MultipartFormMapMessageWriter mapMessageConverter = new MultipartFormMapMessageWriter();

	public MultipartFormParametersMessageWriter() {
	}

	protected MultipartFormParametersMessageWriter(MultipartFormBoundaryGenerator boundaryGenerator) {
		super(boundaryGenerator);
	}

	@Override
	public boolean canWrite(Class<?> type) {
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
