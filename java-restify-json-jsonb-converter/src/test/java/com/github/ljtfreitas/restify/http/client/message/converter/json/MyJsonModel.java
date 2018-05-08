package com.github.ljtfreitas.restify.http.client.message.converter.json;

public class MyJsonModel {

	private String name;

	private int age;

	public MyJsonModel() {
	}

	public MyJsonModel(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}