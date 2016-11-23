package com.github.ljtfreitas.restify.http.client.message.converter.json;

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