package com.restify.http.client.response;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.Header;
import com.restify.http.client.Headers;
import com.restify.http.client.message.HttpMessageConverters;
import com.restify.http.client.message.HttpMessageReader;
import com.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.restify.http.client.request.ExpectedType;
import com.restify.http.contract.ContentType;

public class EndpointResponseReader {

	static final TextPlainMessageConverter ERROR_RESPONSE_MESSAGE_CONVERTER = new TextPlainMessageConverter();

	private final HttpMessageConverters converters;

	public EndpointResponseReader(HttpMessageConverters converters) {
		this.converters = converters;
	}

	public Object read(HttpResponseMessage response, ExpectedType expectedType) {
		if (expectedType.voidType()) {
			return null;

		} else if (expectedType.is(Headers.class)) {
			return response.headers();

		} else if (expectedType.is(HttpResponseMessage.class)) {
			return response;

		} else if (response.readable()) {
			return doRead(response, expectedType);

		} else return null;

	}

	private Object doRead(HttpResponseMessage response, ExpectedType expectedType) {
		EndpointResponse<? extends Object> endpointResponse = ifSuccess(response.code(), r -> r.read(response, expectedType))
				.orElseThrow(() -> onError(response));

		return expectedType.is(EndpointResponse.class) ? endpointResponse : endpointResponse.body();
	}

	private Optional<EndpointResponse<? extends Object>> ifSuccess(EndpointResponseCode code,
			Function<EndpointSuccessResponseReader, EndpointResponse<? extends Object>> consumer) {
		return code.isSucess() ? Optional.of(consumer.apply(new EndpointSuccessResponseReader())) : Optional.empty();
	}

	private RestifyHttpException onError(HttpResponseMessage response) {
		String message = (String) EndpointResponseReader.ERROR_RESPONSE_MESSAGE_CONVERTER.read(response, String.class);

		return new RestifyHttpException("HTTP Status Code: " + response.code() + "\n" + message);
	}

	class EndpointSuccessResponseReader {

		EndpointResponse<? extends Object> read(HttpResponseMessage response, ExpectedType expectedType) {
			Object responseObject = doRead(response, expectedType);

			return new EndpointResponse<>(response.code(), response.headers(), responseObject);
		}

		private Object doRead(HttpResponseMessage response, ExpectedType expectedType) {
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
