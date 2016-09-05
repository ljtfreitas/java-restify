package com.restify.http.client.converter.form.multipart;

import java.util.Collection;

import com.restify.http.client.HttpRequestMessage;

class IterableMultipartFieldSerializer implements MultipartFieldSerializer<Iterable<Object>> {

	private final MultipartFieldSerializers serializers;

	public IterableMultipartFieldSerializer(Collection<MultipartFieldSerializer<?>> serializers) {
		this.serializers = new MultipartFieldSerializers(serializers);
	}

	@Override
	public boolean supports(Class<?> type) {
		return Iterable.class.isAssignableFrom(type);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void write(String boundary, MultipartField<Iterable<Object>> field, HttpRequestMessage httpRequestMessage) {
		Iterable<?> iterable = field.value();

		iterable.forEach(element -> {
			serializers.of(element.getClass())
				.write(boundary, new MultipartField(field.name(), element), httpRequestMessage);
		});
	}

}
