package com.restify.http.client.converter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.restify.http.client.converter.json.JsonMessageConverter;
import com.restify.http.client.converter.text.TextHtmlMessageConverter;
import com.restify.http.client.converter.text.TextPlainMessageConverter;
import com.restify.http.client.converter.xml.JaxBXmlMessageConverter;

public class HttpMessageConverters {

	private final Map<String, HttpMessageConverter> converters = new HashMap<>();

	public HttpMessageConverters() {
		this(new TextPlainMessageConverter(), new TextHtmlMessageConverter(), JsonMessageConverter.available(),
				new JaxBXmlMessageConverter());
	}

	public HttpMessageConverters(HttpMessageConverter...converters) {
		Arrays.stream(converters)
			.forEach(c -> this.converters.put(c.contentType(), c));
	}

	public Optional<HttpMessageConverter> by(String contentType) {
		Optional<HttpMessageConverter> converter = Optional.ofNullable(converters.get(contentType));

		return converter.isPresent() ? converter :
			converters.values().stream()
				.filter(c -> contentType.startsWith(c.contentType()))
					.findFirst();
	}
}
