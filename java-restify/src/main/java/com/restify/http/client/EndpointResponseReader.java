package com.restify.http.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.converter.HttpMessageReader;
import com.restify.http.client.converter.text.TextPlainMessageConverter;
import com.restify.http.contract.ContentType;

public class EndpointResponseReader {

	private static final TextPlainMessageConverter ERROR_RESPONSE_MESSAGE_CONVERTER = new TextPlainMessageConverter();

	private final HttpMessageConverters converters;

	public EndpointResponseReader(HttpMessageConverters converters) {
		this.converters = converters;
	}

	public Object read(EndpointResponse response, Type expectedType) {
		if (isVoid(expectedType)) {
			return null;

		} else if (isEndpointResponse(expectedType)) {
			return response;

		} else if (isHeaders(expectedType)) {
			return response.headers();

		} else if (response.readable()) {
			return doRead(response, expectedType);

		} else {
			return null;
		}
	}

	private boolean isVoid(Type expectedType) {
		return expectedType == Void.TYPE || expectedType == Void.class;
	}

	private boolean isEndpointResponse(Type expectedType) {
		return expectedType instanceof Class
				&& EndpointResponse.class.isAssignableFrom((Class<?>) expectedType);
	}

	private boolean isHeaders(Type expectedType) {
		return expectedType instanceof Class<?>
			&& Headers.class.isAssignableFrom((Class<?>) expectedType);
	}

	private Object doRead(EndpointResponse response, Type expectedType) {
		return ifSuccess(response.code(), r -> r.read(response, expectedType)).orElseThrow(() -> onError(response));
	}

	private Optional<Object> ifSuccess(EndpointResponseCode code, Function<EndpointSuccessResponseReader, Object> consumer) {
		return code.isSucess() ? Optional.of(consumer.apply(new EndpointSuccessResponseReader())) : Optional.empty();
	}

	private RestifyHttpException onError(EndpointResponse response) {
		String message = (String) ERROR_RESPONSE_MESSAGE_CONVERTER.read(String.class, response);
		return new RestifyHttpException("HTTP Status Code: " + response.code() + "\n" + message);
	}

	private class EndpointSuccessResponseReader {

		private Object read(EndpointResponse response, Type expectedType) {
			ContentType contentType = ContentType.of(response.headers().get("Content-Type").map(Header::value).orElse("text/plain"));

			HttpMessageReader<Object> converter = converters.readerOf(contentType, expectedType)
					.orElseThrow(() -> new RestifyHttpMessageReadException("Your request responded a content "
							+ "of type [" + contentType + "], but there is no MessageConverter able to read this message."));

			try {
				Object responseObject = converter.read(expectedType, response);

				response.body().close();

				return responseObject;

			} catch (IOException e) {
				throw new RestifyHttpMessageReadException("Error on try read http response body of type [" + contentType + "]", e);
			}
		}
	}


}
