package com.restify.http.client.response;

import java.io.IOException;
import java.lang.reflect.Type;

import com.restify.http.client.Header;
import com.restify.http.client.Headers;
import com.restify.http.client.message.HttpMessageConverters;
import com.restify.http.client.message.HttpMessageReader;
import com.restify.http.contract.ContentType;
import com.restify.http.contract.metadata.reflection.JavaType;

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
