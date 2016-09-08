package com.restify.http.client;

import java.io.IOException;

import com.restify.http.client.converter.HttpMessageConverter;
import com.restify.http.client.converter.HttpMessageConverters;

public class EndpointRequestWriter {

	private final HttpMessageConverters messageConverters;

	public EndpointRequestWriter(HttpMessageConverters messageConverters) {
		this.messageConverters = messageConverters;
	}

	public void write(EndpointRequest endpointRequest, HttpRequestMessage httpRequestMessage) {
		Object body = endpointRequest.body().orElseThrow(() -> new IllegalArgumentException("Your request does not have a body."));

		Header contentType = endpointRequest.headers().get("Content-Type")
				.orElseThrow(() -> new RestifyHttpMessageWriteException("Your request has a body, but the header [Content-Type] "
						+ "it was not provided."));

		HttpMessageConverter<Object> converter = messageConverters.writerOf(contentType.value(), body.getClass())
				.orElseThrow(() -> new RestifyHttpMessageWriteException("Your request has a [Content-Type] of type [" + contentType + "], "
						+ "but there is no MessageConverter able to write your message."));

		try {
			converter.write(body, httpRequestMessage);

			httpRequestMessage.output().flush();
			httpRequestMessage.output().close();

		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException("Error on try write http body of type [" + contentType + "]", e);
		}
	}
}
