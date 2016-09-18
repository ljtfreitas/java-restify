package com.restify.http.client;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.converter.HttpMessageReader;
import com.restify.http.client.converter.text.TextPlainMessageConverter;
import com.restify.http.contract.ContentType;

public class EndpointResponseReader {

	static final TextPlainMessageConverter ERROR_RESPONSE_MESSAGE_CONVERTER = new TextPlainMessageConverter();

	private final HttpMessageConverters converters;

	public EndpointResponseReader(HttpMessageConverters converters) {
		this.converters = converters;
	}

	public Object read(EndpointResponse response, EndpointExpectedType expectedType) {
		if (expectedType.isVoid()) {
			return null;

		} else if (expectedType.is(EndpointResponse.class)) {
			return response;

		} else if (expectedType.is(Headers.class)) {
			return response.headers();

		} else if (response.readable()) {
			return doRead(response, expectedType);

		} else {
			return null;
		}
	}

	private Object doRead(EndpointResponse response, EndpointExpectedType expectedType) {
		return ifSuccess(response.code(), r -> r.read(response, expectedType)).orElseThrow(() -> onError(response));
	}

	private Optional<Object> ifSuccess(EndpointResponseCode code,
			Function<EndpointSuccessResponseReader, Object> consumer) {
		return code.isSucess() ? Optional.of(consumer.apply(new EndpointSuccessResponseReader())) : Optional.empty();
	}

	private RestifyHttpException onError(EndpointResponse response) {
		String message = (String) EndpointResponseReader.ERROR_RESPONSE_MESSAGE_CONVERTER.read(response, String.class);

		return new RestifyHttpException("HTTP Status Code: " + response.code() + "\n" + message);
	}

	class EndpointSuccessResponseReader {

		Object read(EndpointResponse response, EndpointExpectedType expectedType) {
			Object responseObject = doRead(response, expectedType);

			return expectedType.is(Response.class) ? response.withBody(responseObject) : responseObject;
		}

		private Object doRead(EndpointResponse response, EndpointExpectedType expectedType) {
			ContentType contentType = ContentType
					.of(response.headers().get("Content-Type").map(Header::value).orElse("text/plain"));

			HttpMessageReader<Object> converter = converters.readerOf(contentType, expectedType.type()).orElseThrow(
					() -> new RestifyHttpMessageReadException("Your request responded a content " + "of type ["
							+ contentType + "], but there is no MessageConverter able to read this message."));

			try {
				Object responseObject = converter.read(response, expectedType.type());

				response.body().close();

				return responseObject;

			} catch (IOException e) {
				throw new RestifyHttpMessageReadException(
						"Error on try read http response body of type [" + contentType + "]", e);
			}
		}
	}
}
