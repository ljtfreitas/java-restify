/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.message.converter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.message.ContentType;

public class HttpMessageConverters {

	private final Collection<HttpMessageConverter> converters;

	public HttpMessageConverters(Collection<HttpMessageConverter> converters) {
		this.converters = new ArrayList<>(converters);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<HttpMessageReader<T>> readerOf(ContentType contentType, Type type) {
		return converters.stream()
				.filter(c -> c instanceof HttpMessageReader)
					.map(c -> (HttpMessageReader<T>) c)
						.filter(c -> contentType.compatible(c.contentType()) && c.canRead(type))
							.map(c -> ((HttpMessageReader<T>) c))
								.findFirst();
	}

	@SuppressWarnings("unchecked")
	public <T> Collection<HttpMessageReader<T>> readersOf(Type type) {
		return converters.stream()
				.filter(c -> c instanceof HttpMessageReader)
					.map(c -> (HttpMessageReader<T>) c)
						.filter(c -> c.canRead(type))
							.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<HttpMessageWriter<T>> writerOf(ContentType contentType, Class<?> type) {
		return converters.stream()
				.filter(c -> c instanceof HttpMessageWriter)
					.map(c -> (HttpMessageWriter<T>) c)
						.filter(c -> contentType.compatible(c.contentType()) && c.canWrite(type))
							.map(c -> (HttpMessageWriter<T>) c)
								.findFirst();
	}
}
