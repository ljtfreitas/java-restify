package com.restify.http.client.converter.form.multipart;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.contract.MultipartForm;
import com.restify.http.metadata.MultipartFormObjects;
import com.restify.http.metadata.reflection.JavaAnnotationScanner;

public class MultipartFormObjectMessageWriter extends MultipartFormMessageWriter<Object> {

	private final MultipartFormObjects multipartFormObjects = MultipartFormObjects.cache();

	private final MultipartFormMapMessageWriter mapMessageConverter = new MultipartFormMapMessageWriter();

	public MultipartFormObjectMessageWriter() {
	}

	protected MultipartFormObjectMessageWriter(MultipartFormBoundaryGenerator boundaryGenerator) {
		super(boundaryGenerator);
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return new JavaAnnotationScanner(type).contains(MultipartForm.class);
	}

	@Override
	protected void doWrite(String boundary, Object body, HttpRequestMessage httpRequestMessage) throws IOException {
		Map<String, Object> bodyAsMap = new LinkedHashMap<>();

		multipartFormObjects.of(body.getClass()).fields()
				.forEach(field -> bodyAsMap.put(field.name(), field.valueOn(body)));

		mapMessageConverter.doWrite(boundary, bodyAsMap, httpRequestMessage);
	}
}
