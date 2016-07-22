package com.restify.http.client.converter.text;

public class TextHtmlMessageConverter extends TextMessageConverter {

	@Override
	public String contentType() {
		return "text/html";
	}

}
