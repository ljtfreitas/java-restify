package com.restify.http.client;

import java.io.IOException;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;

public class RestifyEndpointRequestExecutor implements EndpointRequestExecutor {

	private final HttpClientRequestFactory httpClientRequestFactory;
	private final HttpMessageConverters messageConverters;
	private final EndpointResponseReader endpointResponseReader;

	public RestifyEndpointRequestExecutor(HttpClientRequestFactory httpClientRequestFactory, HttpMessageConverters messageConverters) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		this.messageConverters = messageConverters;
		this.endpointResponseReader = new EndpointResponseReader(messageConverters);
	}

	@Override
	public Object execute(EndpointRequest endpointRequest) {
		try (EndpointResponse response = doExecute(endpointRequest)) {
			return responseOf(response, endpointRequest.expectedType());

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

	private EndpointResponse doExecute(EndpointRequest endpointRequest) {
		HttpClientRequest httpClientRequest = httpClientRequestFactory.createOf(endpointRequest);

		endpointRequest.body().ifPresent(b -> {
			String contentType = endpointRequest.headers().get("Content-Type")
					.orElseThrow(() -> new IllegalStateException("Your request has a body, but the header [Content-Type] it was not provided."));

			HttpMessageConverter converter = messageConverters.by(contentType)
					.filter(c -> c.canWrite(b.getClass()))
					.orElseThrow(() -> new IllegalStateException("Your request has a [Content-Type] of type [" + contentType + "], "
							+ "but there is no MessageConverter able to write your message."));

			try {
				converter.write(b, httpClientRequest);

				httpClientRequest.output().flush();
				httpClientRequest.output().close();

			} catch (IOException e) {
				throw new RestifyHttpException(e);
			}
		});

		return httpClientRequest.execute();
	}

	private Object responseOf(EndpointResponse response, Class<?> expectedType) {
		return expectedType == Void.TYPE ? null :
			endpointResponseReader.ifSuccess(response.code()).map(r -> {
				try {
					return r.read(response, expectedType);
				} catch (Exception e) {
					throw new RestifyHttpException(e);
				}

			}).orElseThrow(() -> endpointResponseReader.onError(response));
	}
}
