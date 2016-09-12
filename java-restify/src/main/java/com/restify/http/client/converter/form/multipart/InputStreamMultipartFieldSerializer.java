package com.restify.http.client.converter.form.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

class InputStreamMultipartFieldSerializer extends BaseMultipartFieldSerializer<InputStream> {

	@Override
	public boolean supports(Class<?> type) {
		return InputStream.class.isAssignableFrom(type);
	}

	@Override
	protected ContentDisposition contentDispositionOf(MultipartField<InputStream> field) {
		return new ContentDisposition(field.name(), null);
	}

	@Override
	protected byte[] valueOf(MultipartField<InputStream> field, Charset charset) {
		try {
			return new InputStreamMultipartFieldReader(field.value()).read();
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read data of parameter [" + field.name() + "] (InputStream)", e);
		}
	}

}
