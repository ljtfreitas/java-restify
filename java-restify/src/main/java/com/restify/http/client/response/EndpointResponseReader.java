package com.restify.http.client.response;

import java.io.IOException;
import java.lang.reflect.Type;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.Header;
import com.restify.http.client.Headers;
import com.restify.http.client.message.HttpMessageConverters;
import com.restify.http.client.message.HttpMessageReader;
import com.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.restify.http.contract.ContentType;
import com.restify.http.contract.metadata.reflection.JavaType;

public class EndpointResponseReader {

	static final TextPlainMessageConverter ERROR_RESPONSE_MESSAGE_CONVERTER = new TextPlainMessageConverter();

	private final HttpMessageConverters converters;

	public EndpointResponseReader(HttpMessageConverters converters) {
		this.converters = converters;
	}

	public <T> EndpointResponse<T> read(HttpResponseMessage response, JavaType responseType) {
		if (readableType(responseType) && response.readable()) {
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
				throw doError(response);
			}
		}

		private RestifyHttpException doError(HttpResponseMessage response) {
			String message = (String) EndpointResponseReader.ERROR_RESPONSE_MESSAGE_CONVERTER.read(response, String.class);

			return new RestifyHttpException("HTTP Status Code: " + response.code() + "\n" + message);
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
