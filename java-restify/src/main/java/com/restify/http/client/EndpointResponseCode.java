package com.restify.http.client;

public class EndpointResponseCode {

	private final int code;

	public EndpointResponseCode(int code) {
		this.code = code;
	}

	public boolean isSucess() {
		return (code / 100) == 2;
	}

	public int value() {
		return code;
	}
}
