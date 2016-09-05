package com.restify.http.client.converter.form.multipart;

import java.util.ArrayList;
import java.util.Collection;

class MultipartFieldSerializers {

	private static final MultipartFieldSerializer<Object> FALLBACK_SERIALIZER = new SimpleMultipartFieldSerializer();

	private final Collection<MultipartFieldSerializer<?>> serializers;

	public MultipartFieldSerializers(Collection<MultipartFieldSerializer<?>> serializers) {
		this.serializers = new ArrayList<>(serializers);
	}

	public MultipartFieldSerializers() {
		serializers = new ArrayList<>();

		serializers.add(new FileMultipartFieldSerializer());
		serializers.add(new InputStreamMultipartFieldSerializer());
		serializers.add(new PathMultipartFieldSerializer());
		serializers.add(new MultipartFileFieldSerializer());

		serializers.add(new IterableMultipartFieldSerializer(serializers));
	}

	public MultipartFieldSerializer<?> of(Class<?> type) {
		return serializers.stream()
				.filter(c -> c.supports(type))
					.findFirst()
						.orElseGet(() -> FALLBACK_SERIALIZER);
	}
}
