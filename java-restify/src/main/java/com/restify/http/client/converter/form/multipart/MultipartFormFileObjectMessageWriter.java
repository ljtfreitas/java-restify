package com.restify.http.client.converter.form.multipart;

import java.io.IOException;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.contract.MultipartFile;

public class MultipartFormFileObjectMessageWriter extends MultipartFormMessageWriter<MultipartFile> {

	public MultipartFormFileObjectMessageWriter() {
	}

	protected MultipartFormFileObjectMessageWriter(MultipartFormBoundaryGenerator boundaryGenerator) {
		super(boundaryGenerator);
	}

	@Override
	public boolean canWrite(Class<?> type) {
		return type == MultipartFile.class;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void doWrite(String boundary, MultipartFile body, HttpRequestMessage httpRequestMessage) throws IOException {
		MultipartField field = new MultipartField<MultipartFile>(body.name(), body);

		serializers.of(MultipartFile.class).write(boundary, field, httpRequestMessage);
	}
}
