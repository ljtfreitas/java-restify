package com.restify.http.client.converter.text;

public class TextPlainMessageConverter extends TextMessageConverter {

	@Override
	public String contentType() {
		return "text/plain";
	}

}
