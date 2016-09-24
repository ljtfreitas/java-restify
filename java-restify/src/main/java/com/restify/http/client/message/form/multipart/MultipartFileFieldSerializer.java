package com.restify.http.client.message.form.multipart;

import java.io.IOException;
import java.nio.charset.Charset;

import com.restify.http.contract.ContentType;
import com.restify.http.contract.MultipartFile;

class MultipartFileFieldSerializer extends BaseMultipartFieldSerializer<MultipartFile> {

	@Override
	public boolean supports(Class<?> type) {
		return type == MultipartFile.class;
	}

	@Override
	protected ContentDisposition contentDispositionOf(MultipartField<MultipartFile> field) {
		return new ContentDisposition(field.name(), field.value().fileName().orElse(null));
	}

	@Override
	protected String contentTypeOf(MultipartFile value) {
		return value.contentType().map(ContentType::name).orElse(null);
	}

	@Override
	protected byte[] valueOf(MultipartField<MultipartFile> field, Charset charset) {
		try {
			return new InputStreamMultipartFieldReader(field.value().content()).read();
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Cannot read data of parameter [" + field.name() + "] (MultipartFileField)", e);
		}
	}

}
