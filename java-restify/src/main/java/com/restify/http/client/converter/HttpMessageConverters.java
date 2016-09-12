package com.restify.http.client.converter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.restify.http.client.converter.form.FormURLEncodedFormObjectMessageConverter;
import com.restify.http.client.converter.form.FormURLEncodedMapMessageConverter;
import com.restify.http.client.converter.form.FormURLEncodedParametersMessageConverter;
import com.restify.http.client.converter.form.multipart.MultipartFormFileObjectMessageWriter;
import com.restify.http.client.converter.form.multipart.MultipartFormMapMessageWriter;
import com.restify.http.client.converter.form.multipart.MultipartFormObjectMessageWriter;
import com.restify.http.client.converter.form.multipart.MultipartFormParametersMessageWriter;
import com.restify.http.client.converter.json.JsonMessageConverter;
import com.restify.http.client.converter.text.ScalarMessageConverter;
import com.restify.http.client.converter.text.TextHtmlMessageConverter;
import com.restify.http.client.converter.text.TextPlainMessageConverter;
import com.restify.http.client.converter.xml.JaxbXmlMessageConverter;
import com.restify.http.contract.ContentType;

public class HttpMessageConverters {

	private final Collection<HttpMessageConverter> converters = new ArrayList<>();

	public HttpMessageConverters(HttpMessageConverter...converters) {
		Arrays.stream(converters)
			.forEach(c -> this.converters.add(c));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> Optional<HttpMessageReader<T>> readerOf(ContentType contentType, Type type) {
		return converters.stream()
				.filter(c -> c instanceof HttpMessageReader)
					.map(c -> (HttpMessageReader) c)
						.filter(c -> contentType.is(c.contentType()) && c.canRead(type))
							.map(c -> ((HttpMessageReader<T>) c))
								.findFirst();
	}

	@SuppressWarnings("rawtypes")
	public <T> Collection<HttpMessageReader<T>> readersOf(Type type) {
		return converters.stream()
				.filter(c -> c instanceof HttpMessageReader)
					.map(c -> (HttpMessageReader) c)
						.filter(c -> c.canRead(type))
							.collect(Collectors.toList());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> Optional<HttpMessageWriter<T>> writerOf(ContentType contentType, Class<?> type) {
		return converters.stream()
				.filter(c -> c instanceof HttpMessageWriter)
					.map(c -> (HttpMessageWriter) c)
						.filter(c -> contentType.is(c.contentType()) && c.canWrite(type))
							.map(c -> (HttpMessageWriter<T>) c)
								.findFirst();
	}

	public static HttpMessageConverters build() {
		return new HttpMessageConverters(JsonMessageConverter.available(), 
			new JaxbXmlMessageConverter<Object>(),
			new TextPlainMessageConverter(), 
			new TextHtmlMessageConverter(),
			new ScalarMessageConverter(),
			new FormURLEncodedParametersMessageConverter(), 
			new FormURLEncodedFormObjectMessageConverter(),
			new FormURLEncodedMapMessageConverter(),
			new MultipartFormParametersMessageWriter(),
			new MultipartFormObjectMessageWriter(),
			new MultipartFormFileObjectMessageWriter(),
			new MultipartFormMapMessageWriter());
	}
}
