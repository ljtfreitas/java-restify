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
package com.github.ljtfreitas.restify.http.client.response;

import java.io.IOException;
import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.client.message.ContentType;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReadException;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageReader;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class EndpointResponseReader {

	private final HttpMessageConverters converters;
	private final EndpointResponseErrorFallback endpointResponseErrorFallback;

	public EndpointResponseReader(HttpMessageConverters converters) {
		this(converters, new DefaultEndpointResponseErrorFallback());
	}

	public EndpointResponseReader(HttpMessageConverters converters, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.converters = converters;
		this.endpointResponseErrorFallback = endpointResponseErrorFallback;
	}

	public <T> EndpointResponse<T> read(HttpResponseMessage response, JavaType responseType) {
		if (response.status().isError()) {
			return endpointResponseErrorFallback.onError(response, responseType);

		} else if (readableType(responseType) && response.readable()) {
			return doRead(response, responseType);

		} else {
			return empty(response);
		}
	}

	private boolean readableType(JavaType responseType) {
		return !(responseType.voidType() || responseType.is(Headers.class));
	}

	private <T> EndpointResponse<T> doRead(HttpResponseMessage response, JavaType responseType) {
		return new EndpointResponseContentReader<T>().read(response, responseType);
	}

	private class EndpointResponseContentReader<T> {
		private EndpointResponse<T> read(HttpResponseMessage response, JavaType responseType) {
			StatusCode responseStatus = response.status();

			if (responseStatus.isSucessful()) {
				return doRead(response, responseType.unwrap());

			} else if (responseStatus.isRedirection()) {
				return empty(response);

			} else {
				return endpointResponseErrorFallback.onError(response, null);
			}
		}

		@SuppressWarnings("unchecked")
		private EndpointResponse<T> doRead(HttpResponseMessage response, Type responseType) {
			ContentType contentType = response.headers().get("Content-Type")
					.map(Header::value)
					.map(ContentType::of)
						.orElseThrow(
							() -> new HttpMessageReadException("Your request responded a content, but Content-Type header is not present."));

			HttpMessageReader<Object> converter = converters.readerOf(contentType, responseType).orElseThrow(
					() -> new HttpMessageReadException("Your request responded a content " + "of type ["
							+ contentType + "], but there is no MessageConverter able to read this message."));

			try {
				T responseObject = (T) converter.read(response, responseType);
				response.body().close();

				return new EndpointResponse<>(response.status(), response.headers(), responseObject);

			} catch (IOException e) {
				throw new HttpMessageReadException(
						"Error on read HTTP response body of type [" + contentType + "]", e);
			}
		}
	}
	
	private <T> EndpointResponse<T> empty(HttpResponseMessage response) {
		return EndpointResponse.empty(response.status(), response.headers());
	}
}
