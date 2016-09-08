package com.restify.http.client.converter.text;

public class TextHtmlMessageConverter extends TextMessageConverter {

	private static final String TEXT_HTML = "text/html";

	@Override
	public String contentType() {
		return TEXT_HTML;
	}

}
