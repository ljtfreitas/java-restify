package com.restify.http.client.request.apache.httpclient;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.restify.http.client.Headers;
import com.restify.http.client.response.BaseHttpResponseMessage;
import com.restify.http.client.response.StatusCode;

class ApacheHttpClientResponse extends BaseHttpResponseMessage {

	private final HttpEntity entity;
	private final HttpResponse httpResponse;

	ApacheHttpClientResponse(StatusCode statusCode, Headers headers, InputStream body,
			HttpEntity entity, HttpResponse httpResponse) {
		super(statusCode, headers, body);
		this.entity = entity;
		this.httpResponse = httpResponse;
	}

	@Override
	public void close() throws IOException {
		try {
            EntityUtils.consume(entity);
        }
		finally {
			if (httpResponse instanceof Closeable) {
				((Closeable) httpResponse).close();
			}
        }
	}

}
