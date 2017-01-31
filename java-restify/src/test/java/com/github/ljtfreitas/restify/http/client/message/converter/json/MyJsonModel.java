package com.github.ljtfreitas.restify.http.client.message.converter.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
class MyJsonModel {

	String name;
	int age;

	MyJsonModel() {
	}

	MyJsonModel(String name, int age) {
		this.name = name;
		this.age = age;
	}
}