package com.restify.http.client.message.form.multipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

class PathMultipartFieldSerializer extends BaseMultipartFieldSerializer<Path> {

	@Override
	public boolean supports(Class<?> type) {
		return Path.class.isAssignableFrom(type);
	}

	@Override
	protected ContentDisposition contentDispositionOf(MultipartField<Path> field) {
		return new ContentDisposition(field.name(), field.value().getFileName().toString());
	}

	@Override
	protected String contentTypeOf(Path value) {
		try {
			return Files.probeContentType(value);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	protected byte[] valueOf(MultipartField<Path> field, Charset charset) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			Files.copy(field.value(), output);
			return output.toByteArray();

		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read data of parameter [" + field.name() + "] (Path)", e);
		}
	}

}
