package com.restify.http.client.converter.form.multipart;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.restify.http.client.HttpRequestMessage;
import com.restify.http.client.HttpResponseMessage;
import com.restify.http.client.RestifyHttpMessageReadException;
import com.restify.http.client.RestifyHttpMessageWriteException;
import com.restify.http.client.converter.HttpMessageConverter;

abstract class MultipartFormMessageConverter<T> implements HttpMessageConverter<T> {

	private static final String MULTIPART_FORM_DATA = "multipart/form-data";

	protected final MultipartFieldSerializers serializers = new MultipartFieldSerializers();

	private final MultipartFormBoundaryGenerator boundaryGenerator;

	public MultipartFormMessageConverter() {
		this.boundaryGenerator = new UUIDMultipartFormBoundaryGenerator();
	}

	protected MultipartFormMessageConverter(MultipartFormBoundaryGenerator boundaryGenerator) {
		this.boundaryGenerator = boundaryGenerator;
	}

	@Override
	public String contentType() {
		return MULTIPART_FORM_DATA;
	}

	@Override
	public boolean readerOf(Type type) {
		return false;
	}

	@Override
	public T read(Type expectedType, HttpResponseMessage httpResponseMessage) throws RestifyHttpMessageReadException {
		throw new RestifyHttpMessageReadException(
				"Cannot read multipart/form-data response. But...this response format make sense?");
	}

	@Override
	public void write(T body, HttpRequestMessage httpRequestMessage) throws RestifyHttpMessageWriteException {
		try {
			String boundary = boundaryGenerator.generate();

			httpRequestMessage.headers().replace("Content-Type", "multipart/form-data; boundary=" + "----" + boundary);

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

	protected abstract void doWrite(String boundary, T body, HttpRequestMessage httpRequestMessage) throws IOException;
}
