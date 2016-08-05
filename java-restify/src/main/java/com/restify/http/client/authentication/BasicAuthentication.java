package com.restify.http.client.authentication;

import java.util.Base64;

public class BasicAuthentication implements Authentication {

	private final String user;
	private final String password;

	public BasicAuthentication(String user, String password) {
		this.user = user;
		this.password = password;
	}

	@Override
	public String content() {
		String content = user + ":" + password;
		return "Basic " + Base64.getEncoder().encodeToString(content.getBytes());
	}
}
