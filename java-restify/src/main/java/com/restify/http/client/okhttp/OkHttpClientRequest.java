package com.restify.http.client.okhttp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.EndpointRequest;
import com.restify.http.client.EndpointResponse;
import com.restify.http.client.EndpointResponseCode;
import com.restify.http.client.Headers;
import com.restify.http.client.HttpClientRequest;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpClientRequest implements HttpClientRequest {

	private final OkHttpClient okHttpClient;
	private final EndpointRequest endpointRequest;
	private final String charset;

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

	public OkHttpClientRequest(OkHttpClient okHttpClient, EndpointRequest endpointRequest, String charset) {
		this.okHttpClient = okHttpClient;
		this.endpointRequest = endpointRequest;
		this.charset = charset;
	}

	@Override
	public OutputStream output() {
		return outputStream;
	}

	@Override
	public String charset() {
		return charset;
	}

	@Override
	public EndpointResponse execute() throws RestifyHttpException {
		MediaType contentType = endpointRequest.headers().get("Content-Type").map(value -> MediaType.parse(value))
				.orElse(null);

		byte[] content = outputStream.toByteArray();

		try {
			RequestBody body = (content.length > 0 ? RequestBody.create(contentType, content) : null);

			Request.Builder builder = new Request.Builder();

			builder.url(endpointRequest.endpoint().toURL())
				.method(endpointRequest.method(), body);

			endpointRequest.headers().all().forEach(h -> builder.addHeader(h.name(), h.value()));

			Request request = builder.build();

			return responseOf(okHttpClient.newCall(request).execute());

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}

	private EndpointResponse responseOf(Response response) {
		EndpointResponseCode code = new EndpointResponseCode(response.code());

		Headers headers = new Headers();
		response.headers().names().forEach(name -> headers.put(name, response.headers(name)));

		InputStream stream = response.body().byteStream();

		return new EndpointResponse(code, headers, stream) {

			@Override
			public void close() throws IOException {
				response.body().close();
			}
		};
	}

}
