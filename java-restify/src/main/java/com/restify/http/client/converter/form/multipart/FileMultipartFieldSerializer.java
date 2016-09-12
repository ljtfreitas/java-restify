package com.restify.http.client.converter.form.multipart;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

class FileMultipartFieldSerializer extends BaseMultipartFieldSerializer<File> {

	@Override
	public boolean supports(Class<?> type) {
		return type == File.class;
	}

	@Override
	protected ContentDisposition contentDispositionOf(MultipartField<File> field) {
		return new ContentDisposition(field.name(), field.value().getName());
	}

	@Override
	protected String contentTypeOf(File value) {
		try {
			return Files.probeContentType(value.toPath());
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	protected byte[] valueOf(MultipartField<File> field, Charset charset) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			Files.copy(field.value().toPath(), output);
			return output.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read data of parameter [" + field.name() + "] (File)", e);
		}
	}

}
