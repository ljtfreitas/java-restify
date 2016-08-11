package com.restify.http.client;

import java.io.IOException;
import java.lang.reflect.Type;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;
import com.restify.http.client.interceptor.EndpointRequestInterceptorStack;

public class DefaultEndpointRequestExecutor implements EndpointRequestExecutor {

	private final HttpClientRequestFactory httpClientRequestFactory;
	private final HttpMessageConverters messageConverters;
	private final EndpointResponseReader endpointResponseReader;
	private final EndpointRequestInterceptorStack endpointRequestInterceptorStack;

	public DefaultEndpointRequestExecutor(HttpClientRequestFactory httpClientRequestFactory, HttpMessageConverters messageConverters,
			EndpointRequestInterceptorStack endpointRequestInterceptorStack) {
		this.httpClientRequestFactory = httpClientRequestFactory;
		this.messageConverters = messageConverters;
		this.endpointRequestInterceptorStack = endpointRequestInterceptorStack;
		this.endpointResponseReader = new EndpointResponseReader(messageConverters);
	}

	@Override
	public Object execute(EndpointRequest endpointRequest) {
		try (EndpointResponse response = doExecute(intercepts(endpointRequest))) {
			return responseOf(response, endpointRequest.expectedType());

		} catch (Exception e) {
			throw new RestifyHttpException(e);
		}
	}

	private EndpointResponse doExecute(EndpointRequest endpointRequest) {
		HttpClientRequest httpClientRequest = httpClientRequestFactory.createOf(endpointRequest);

		endpointRequest.body().ifPresent(b -> {
			Header contentType = endpointRequest.headers().get("Content-Type")
					.orElseThrow(() -> new RestifyHttpMessageWriteException("Your request has a body, but the header [Content-Type] "
							+ "it was not provided."));

			HttpMessageConverter<Object> converter = messageConverters.writerOf(contentType.value(), b.getClass())
					.orElseThrow(() -> new RestifyHttpMessageWriteException("Your request has a [Content-Type] of type [" + contentType + "], "
							+ "but there is no MessageConverter able to write your message."));

			try {
				converter.write(b, httpClientRequest);

				httpClientRequest.output().flush();
				httpClientRequest.output().close();

			} catch (IOException e) {
				throw new RestifyHttpMessageWriteException("Error on try write http body of type [" + contentType + "]", e);
			}
		});

		return httpClientRequest.execute();
	}

	private EndpointRequest intercepts(EndpointRequest endpointRequest) {
		return endpointRequestInterceptorStack.apply(endpointRequest);
	}

	private Object responseOf(EndpointResponse response, Type expectedType) {
		return isVoid(expectedType) ? null :
			endpointResponseReader.ifSuccess(response.code(), r -> {
				return r.read(response, expectedType);

			}).orElseThrow(() -> endpointResponseReader.onError(response));
	}

	private boolean isVoid(Type expectedType) {
		return expectedType == Void.TYPE || expectedType == Void.class;
	}
}
