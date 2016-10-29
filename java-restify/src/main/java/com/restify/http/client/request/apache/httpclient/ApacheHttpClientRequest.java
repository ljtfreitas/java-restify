package com.restify.http.client.request.apache.httpclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;

import com.restify.http.RestifyHttpException;
import com.restify.http.client.Headers;
import com.restify.http.client.request.HttpClientRequest;
import com.restify.http.client.response.StatusCode;
import com.restify.http.client.response.HttpResponseMessage;

public class ApacheHttpClientRequest implements HttpClientRequest {

	private final HttpClient httpClient;
	private final HttpUriRequest httpRequest;
	private final HttpContext httpContext;
	private final Charset charset;
	private final Headers headers;

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

	public ApacheHttpClientRequest(HttpClient httpClient, HttpUriRequest httpRequest, HttpContext httpContext, Charset charset, Headers headers) {
		this.httpClient = httpClient;
		this.httpRequest = httpRequest;
		this.httpContext = httpContext;
		this.charset = charset;
		this.headers = headers;
	}

	@Override
	public HttpResponseMessage execute() throws RestifyHttpException {
		headers.all().forEach(h -> httpRequest.addHeader(h.name(), h.value()));

		if (httpRequest instanceof HttpEntityEnclosingRequest) {
			HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
			HttpEntity requestEntity = new ByteArrayEntity(outputStream.toByteArray());
			entityEnclosingRequest.setEntity(requestEntity);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpRequest, httpContext);

			return responseOf(httpResponse);

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}

	}

	private ApacheHttpClientResponse responseOf(HttpResponse httpResponse) throws IOException {
		StatusCode statusCode = StatusCode.of(httpResponse.getStatusLine().getStatusCode());

		Headers headers = new Headers();
		Arrays.stream(httpResponse.getAllHeaders())
			.forEach(h -> headers.add(new com.restify.http.client.Header(h.getName(), h.getValue())));

		HttpEntity entity = httpResponse.getEntity();

		InputStream stream = entity != null ? entity.getContent()
				: new ByteArrayInputStream(new byte[0]);

		return new ApacheHttpClientResponse(statusCode, headers, stream, entity, httpResponse);
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
		return headers;
	}
}
