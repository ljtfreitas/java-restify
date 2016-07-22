package com.restify.http.client;

import java.io.IOException;
import java.util.Optional;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;

public class EndpointResponseReader {

	private final HttpMessageConverters converters;

	public EndpointResponseReader(HttpMessageConverters converters) {
		this.converters = converters;
	}

	public Optional<EndpointSuccessResponseReader> ifSuccess(EndpointResponseCode code) {
		return code.isSucess() ? Optional.of(new EndpointSuccessResponseReader()) : Optional.empty();
	}

	public RestifyHttpException onError(EndpointResponse code) {
		return new RestifyHttpException("");
	}

	public class EndpointSuccessResponseReader {

		public Object read(EndpointResponse response, Class<?> expectedType) {
			String contentType = response.headers().get("Content-Type").orElse("text/plain");

			HttpMessageConverter converter = converters.by(contentType)
					.filter(c -> c.canRead(expectedType))
					.orElseThrow(() -> new IllegalStateException("Your request responded a content "
							+ "of type [" + contentType + "], but there is no MessageConverter able to read this message."));

			try {
				Object responseObject = converter.read(expectedType, response);

				response.input().close();

				return responseObject;

			} catch (IOException e) {
				throw new RestifyHttpException(e);
			}
		}
	}


}
