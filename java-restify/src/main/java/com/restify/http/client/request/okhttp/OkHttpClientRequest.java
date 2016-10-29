package com.restify.http.client.request.okhttp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.Headers;
import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.HttpClientRequest;
import com.restify.http.client.response.StatusCode;
import com.restify.http.client.response.HttpResponseMessage;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpClientRequest implements HttpClientRequest {

	private final OkHttpClient okHttpClient;
	private final EndpointRequest endpointRequest;
	private final Charset charset;

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

	public OkHttpClientRequest(OkHttpClient okHttpClient, EndpointRequest endpointRequest, Charset charset) {
		this.okHttpClient = okHttpClient;
		this.endpointRequest = endpointRequest;
		this.charset = charset;
	}

	@Override
	public OutputStream output() {
		return outputStream;
	}

	@Override
	public Charset charset() {
		return charset;
	}

	@Override
	public Headers headers() {
		return endpointRequest.headers();
	}

	@Override
	public HttpResponseMessage execute() throws RestifyHttpException {
		MediaType contentType = endpointRequest.headers().get("Content-Type").map(header -> MediaType.parse(header.value()))
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

	private OkHttpClientResponse responseOf(Response response) {
		StatusCode statusCode = StatusCode.of(response.code());

		Headers headers = new Headers();
		response.headers().names().forEach(name -> headers.put(name, response.headers(name)));

		InputStream stream = response.body().byteStream();

		return new OkHttpClientResponse(statusCode, headers, stream, response);
	}

}
