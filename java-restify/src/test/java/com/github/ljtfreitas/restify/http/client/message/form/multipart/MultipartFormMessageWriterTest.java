package com.github.ljtfreitas.restify.http.client.message.form.multipart;

import static com.github.ljtfreitas.restify.http.client.header.Headers.CONTENT_TYPE;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.message.form.multipart.MultipartFormMessageWriter;
import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.SimpleHttpRequestMessage;

public class MultipartFormMessageWriterTest {

	private MultipartFormMessageWriter<Object> writer;

	@Before
	public void setup() {
		writer = new MultipartFormMessageWriter<Object>(new SimpleMultipartFormBoundaryGenerator("abc1234")) {
			@Override
			public boolean canWrite(Class<?> type) {
				return true;
			}

			@Override
			protected void doWrite(String boundary, Object body, HttpRequestMessage httpRequestMessage)
					throws IOException {
				OutputStream output = httpRequestMessage.output();

				output.write(body.toString().getBytes());
				output.flush();
			}
		};
	}

	@Test
	public void shouldAddBoundaryParameterToContentTypeHeader() {
		Headers headers = new Headers(new Header(CONTENT_TYPE, "multipart/form-data"));

		HttpRequestMessage httpRequestMessage = new SimpleHttpRequestMessage(headers);

		writer.write("MultipartFormMessageWriterTest", httpRequestMessage);

		assertEquals("multipart/form-data; boundary=----abc1234", headers.get(CONTENT_TYPE).get().value());
	}

	@Test
	public void shouldWriteHttpRequestBody() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		HttpRequestMessage httpRequestMessage = new SimpleHttpRequestMessage(output);

		writer.write("MultipartFormMessageWriterTest", httpRequestMessage);

		String expectedBody = "MultipartFormMessageWriterTest\r\n------abc1234--";

		assertEquals(expectedBody, output.toString());
	}
}
