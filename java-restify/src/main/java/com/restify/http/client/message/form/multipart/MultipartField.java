package com.restify.http.client.message.form.multipart;

class MultipartField<T> {

	private final String name;
	private final T value;

	public MultipartField(String name, T value) {
		this.name = name;
		this.value = value;
	}

	public String name() {
		return name;
	}

	public T value() {
		return value;
	}

}
