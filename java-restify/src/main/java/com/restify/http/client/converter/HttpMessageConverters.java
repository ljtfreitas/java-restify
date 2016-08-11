package com.restify.http.client.converter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.restify.http.client.converter.form.FormURLEncodedMapMessageConverter;
import com.restify.http.client.converter.form.FormURLEncodedParametersMessageConverter;
import com.restify.http.client.converter.json.JsonMessageConverter;
import com.restify.http.client.converter.text.TextHtmlMessageConverter;
import com.restify.http.client.converter.text.TextPlainMessageConverter;
import com.restify.http.client.converter.xml.JaxBXmlMessageConverter;

public class HttpMessageConverters {

	private final Collection<HttpMessageConverter<?>> converters = new ArrayList<>();

	public HttpMessageConverters(HttpMessageConverter<?>...converters) {
		Arrays.stream(converters)
			.forEach(c -> this.converters.add(c));
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<HttpMessageConverter<T>> readerOf(String contentType, Type type) {
		return converters.stream()
				.filter(c -> (c.contentType().equals(contentType) || contentType.startsWith(c.contentType()) && c.canRead(type)))
				.map(c -> ((HttpMessageConverter<T>) c))
					.findFirst();
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<HttpMessageConverter<T>> readersOf(Type type) {
		return converters.stream()
				.filter(c ->  c.canRead(type))
				.map(c -> ((HttpMessageConverter<T>) c))
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<HttpMessageConverter<T>> writerOf(String contentType, Class<?> type) {
		return converters.stream()
				.filter(c -> (contentType == null || c.contentType().equals(contentType) || contentType.startsWith(c.contentType()) 
								&& c.canWrite(type)))
				.map(c -> ((HttpMessageConverter<T>) c))
					.findFirst();
	}
	
	public static HttpMessageConverters build() {
		return new HttpMessageConverters(JsonMessageConverter.available(), 
			new JaxBXmlMessageConverter<Object>(),
			new TextPlainMessageConverter(), 
			new TextHtmlMessageConverter(),
			new FormURLEncodedParametersMessageConverter(), 
			new FormURLEncodedMapMessageConverter());
	}
}
