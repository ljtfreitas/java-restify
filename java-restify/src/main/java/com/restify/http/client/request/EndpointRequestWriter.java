package com.restify.http.client.request;

import java.io.IOException;

import com.restify.http.client.Header;
import com.restify.http.client.message.HttpMessageConverters;
import com.restify.http.client.message.HttpMessageWriter;
import com.restify.http.contract.ContentType;

import static com.restify.http.client.Headers.CONTENT_TYPE;

public class EndpointRequestWriter {

	private final HttpMessageConverters messageConverters;

	public EndpointRequestWriter(HttpMessageConverters messageConverters) {
		this.messageConverters = messageConverters;
	}

	public void write(EndpointRequest endpointRequest, HttpRequestMessage httpRequestMessage) {
		Object body = endpointRequest.body()
				.orElseThrow(() -> new IllegalArgumentException("Your request does not have a body."));

		doWrite(body, httpRequestMessage);
	}

	private void doWrite(Object body, HttpRequestMessage httpRequestMessage) {
		ContentType contentType = contentTypeOf(httpRequestMessage);

		HttpMessageWriter<Object> writer = writerOf(contentType, body);

		try {
			writer.write(body, httpRequestMessage);

			httpRequestMessage.output().flush();
			httpRequestMessage.output().close();

		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException("Error on write HTTP body of type [" + contentType + "].", e);
		}
	}

	private ContentType contentTypeOf(HttpRequestMessage httpRequestMessage) {
		Header contentTypeHeader = httpRequestMessage.headers().get(CONTENT_TYPE)
				.orElseThrow(() -> new RestifyHttpMessageWriteException("Your request has a body, but the header [Content-Type] "
						+ "it was not provided."));

		ContentType contentType = ContentType.of(contentTypeHeader.value());

		if (!contentType.parameter("charset").isPresent()) {
			contentType = contentType.newParameter("charset", httpRequestMessage.charset().name());
			httpRequestMessage.headers().replace(CONTENT_TYPE, contentType.toString());
		}

		return contentType;
	}

	private HttpMessageWriter<Object> writerOf(ContentType contentType, Object body) {
		return messageConverters.writerOf(contentType, body.getClass())
				.orElseThrow(() -> new RestifyHttpMessageWriteException("Your request has a [Content-Type] "
					+ "of type [" + contentType.name() + "], but there is no MessageConverter able to write your message."));
	}
}
