package com.restify.http.client.converter.form.multipart;

import java.nio.charset.Charset;

class SimpleMultipartFieldSerializer extends BaseMultipartFieldSerializer<Object> {

	@Override
	public boolean supports(Class<?> type) {
		return true;
	}

	@Override
	protected ContentDisposition contentDispositionOf(MultipartField<Object> field) {
		return new ContentDisposition(field.name(), null);
	}

	@Override
	protected byte[] valueOf(MultipartField<Object> field, String charset) {
		return field.value().toString().getBytes(Charset.forName(charset));
	}

	@Override
	protected String contentTypeOf(Object value) {
		return "text/plain";
	}
}
