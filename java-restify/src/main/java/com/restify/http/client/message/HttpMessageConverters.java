package com.restify.http.client.message;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.restify.http.contract.ContentType;

public class HttpMessageConverters {

	private final Collection<HttpMessageConverter> converters;

	public HttpMessageConverters(Collection<HttpMessageConverter> converters) {
		this.converters = new ArrayList<>(converters);
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
}
