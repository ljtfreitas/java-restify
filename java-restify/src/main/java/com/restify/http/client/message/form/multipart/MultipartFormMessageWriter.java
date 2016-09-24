package com.restify.http.client.message.form.multipart;

import static com.restify.http.client.Headers.CONTENT_TYPE;

import java.io.IOException;
import java.io.OutputStream;

import com.restify.http.client.Header;
import com.restify.http.client.message.HttpMessageWriter;
import com.restify.http.client.request.HttpRequestMessage;
import com.restify.http.client.request.RestifyHttpMessageWriteException;
import com.restify.http.contract.ContentType;

abstract class MultipartFormMessageWriter<T> implements HttpMessageWriter<T> {

	private static final String MULTIPART_FORM_DATA = "multipart/form-data";

	protected final MultipartFieldSerializers serializers = new MultipartFieldSerializers();

	private final MultipartFormBoundaryGenerator boundaryGenerator;

	public MultipartFormMessageWriter() {
		this.boundaryGenerator = new UUIDMultipartFormBoundaryGenerator();
	}

	protected MultipartFormMessageWriter(MultipartFormBoundaryGenerator boundaryGenerator) {
		this.boundaryGenerator = boundaryGenerator;
	}

	@Override
	public String contentType() {
		return MULTIPART_FORM_DATA;
	}

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		try {
			String boundary = boundaryGenerator.generate();

			addToContentType(boundary, httpRequestMessage);

			doWrite("------" + boundary, body, httpRequestMessage);

			OutputStream output = httpRequestMessage.output();
			output.write('\r');
			output.write('\n');
			output.write(("------" + boundary + "--").getBytes());

			output.flush();
			output.close();

		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException(e);
		}
	}

	private void addToContentType(String boundary, HttpRequestMessage httpRequestMessage) {
		Header contentTypeHeader = httpRequestMessage.headers().get(CONTENT_TYPE)
				.orElseGet(() -> new Header(CONTENT_TYPE, MULTIPART_FORM_DATA));

		ContentType contentType = ContentType.of(contentTypeHeader.value());

		httpRequestMessage.headers().replace(CONTENT_TYPE, contentType.newParameter("boundary", "----" + boundary).toString());
	}

	protected abstract void doWrite(String boundary, T body, HttpRequestMessage httpRequestMessage) throws IOException;
}
