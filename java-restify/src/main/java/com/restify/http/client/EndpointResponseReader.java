package com.restify.http.client;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.converter.text.TextPlainMessageConverter;

public class EndpointResponseReader {

	private final HttpMessageConverters converters;
	private final TextPlainMessageConverter errorResponseMessageConverter = new TextPlainMessageConverter();

	public EndpointResponseReader(HttpMessageConverters converters) {
		this.converters = converters;
	}

	public Optional<Object> ifSuccess(EndpointResponseCode code, Function<EndpointSuccessResponseReader, Object> consumer) {
		return code.isSucess() ? Optional.of(consumer.apply(new EndpointSuccessResponseReader())) : Optional.empty();
	}

	public RestifyHttpException onError(EndpointResponse response) {
		String message = (String) errorResponseMessageConverter.read(String.class, response);
		return new RestifyHttpException(response.code() + " " + message);
	}

	public class EndpointSuccessResponseReader {

		public Object read(EndpointResponse response, Type expectedType) {
			String contentType = response.headers().get("Content-Type").map(Header::value).orElse("text/plain");

			HttpMessageConverter<Object> converter = converters.readerOf(contentType, expectedType)
					.orElseThrow(() -> new RestifyHttpMessageReadException("Your request responded a content "
							+ "of type [" + contentType + "], but there is no MessageConverter able to read this message."));

			try {
				Object responseObject = converter.read(expectedType, response);

				response.input().close();

				return responseObject;

			} catch (IOException e) {
				throw new RestifyHttpMessageReadException("Error on try read http response body of type [" + contentType + "]", e);
			}
		}
	}


}
