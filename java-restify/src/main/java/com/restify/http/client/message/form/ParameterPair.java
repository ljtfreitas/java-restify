package com.restify.http.client.message.form;

class ParameterPair {

	private String name;
	private String value;

	ParameterPair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	String name() {
		return name;
	}

	String value() {
		return value;
	}

	@Override
	public String toString() {
		return name + "=" + value;
	}
}
