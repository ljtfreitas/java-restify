package com.restify.http.client.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import com.restify.http.client.converter.form.FormURLEncodedMapMessageConverter;
import com.restify.http.client.converter.form.FormURLEncodedParametersMessageConverter;
import com.restify.http.client.converter.json.JsonMessageConverter;
import com.restify.http.client.converter.text.TextHtmlMessageConverter;
import com.restify.http.client.converter.text.TextPlainMessageConverter;
import com.restify.http.client.converter.xml.JaxBXmlMessageConverter;

public class HttpMessageConverters {

	private final Collection<HttpMessageConverter<?>> converters = new ArrayList<>();

	public HttpMessageConverters() {
		this.converters.add(JsonMessageConverter.available());
		this.converters.add(new TextPlainMessageConverter());
		this.converters.add(new TextHtmlMessageConverter());
		this.converters.add(new JaxBXmlMessageConverter<Object>());
		this.converters.add(new FormURLEncodedParametersMessageConverter());
		this.converters.add(new FormURLEncodedMapMessageConverter());
	}

	public HttpMessageConverters(HttpMessageConverter<?>...converters) {
		Arrays.stream(converters)
			.forEach(c -> this.converters.add(c));
	}

	public void add(HttpMessageConverter<?> converter) {
		converters.add(converter);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<HttpMessageConverter<T>> readerOf(String contentType, Class<?> type) {
		return converters.stream()
				.filter(c -> (c.contentType().equals(contentType) || contentType.startsWith(c.contentType()) && c.canRead(type)))
					.map(c -> ((HttpMessageConverter<T>) c))
						.findFirst();
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<HttpMessageConverter<T>> writerOf(String contentType, Class<?> type) {
		return converters.stream()
				.filter(c -> (c.contentType().equals(contentType) || contentType.startsWith(c.contentType()) && c.canWrite(type)))
				.map(c -> ((HttpMessageConverter<T>) c))
					.findFirst();
	}
}
