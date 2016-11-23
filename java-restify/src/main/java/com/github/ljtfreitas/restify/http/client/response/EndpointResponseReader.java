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

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageReader;
import com.github.ljtfreitas.restify.http.contract.ContentType;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;

public class EndpointResponseReader {

	private final HttpMessageConverters converters;
	private final EndpointResponseErrorFallback endpointResponseErrorFallback;

	public EndpointResponseReader(HttpMessageConverters converters, EndpointResponseErrorFallback endpointResponseErrorFallback) {
		this.converters = converters;
		this.endpointResponseErrorFallback = endpointResponseErrorFallback;
	}

	public <T> EndpointResponse<T> read(HttpResponseMessage response, JavaType responseType) {
		if (readableType(responseType) && response.isReadable()) {
			return doRead(response, responseType);
		} else {
			return EndpointResponse.empty(response.code(), response.headers());
		}
	}

	private boolean readableType(JavaType responseType) {
		return !(responseType.voidType() || responseType.is(Headers.class));
	}

	private <T> EndpointResponse<T> doRead(HttpResponseMessage response, JavaType responseType) {
		return new EndpointResponseContentReader<T>().read(response, responseType);
	}

	class EndpointResponseContentReader<T> {
		private EndpointResponse<T> read(HttpResponseMessage response, JavaType responseType) {
			if (response.code().isSucess()) {
				return doRead(response, responseType.unwrap());

			} else {
				return endpointResponseErrorFallback.onError(response);
			}
		}

		@SuppressWarnings("unchecked")
		private EndpointResponse<T> doRead(HttpResponseMessage response, Type responseType) {
			ContentType contentType = ContentType
					.of(response.headers().get("Content-Type").map(Header::value).orElse("text/plain"));

			HttpMessageReader<Object> converter = converters.readerOf(contentType, responseType).orElseThrow(
					() -> new RestifyHttpMessageReadException("Your request responded a content " + "of type ["
							+ contentType + "], but there is no MessageConverter able to read this message."));

			try {
				T responseObject = (T) converter.read(response, responseType);

				response.body().close();

				return new EndpointResponse<>(response.code(), response.headers(), responseObject);

			} catch (IOException e) {
				throw new RestifyHttpMessageReadException(
						"Error on try read http response body of type [" + contentType + "]", e);
			}
		}
	}
}
