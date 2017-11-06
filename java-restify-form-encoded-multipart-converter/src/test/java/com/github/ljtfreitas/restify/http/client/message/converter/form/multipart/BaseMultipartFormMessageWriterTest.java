package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import static com.github.ljtfreitas.restify.http.client.header.Headers.CONTENT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.form.SimpleHttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.request.HttpRequestMessage;

public class BaseMultipartFormMessageWriterTest {

	private BaseMultipartFormMessageWriter<Object> writer;

	@Before
	public void setup() {
		writer = new BaseMultipartFormMessageWriter<Object>(new SimpleMultipartFormBoundaryGenerator("abc1234")) {
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

		Header expected = Header.contentType("multipart/form-data; boundary=----abc1234");

		SimpleHttpRequestMessage source = spy(new SimpleHttpRequestMessage(headers));
		SimpleHttpRequestMessage modified = new SimpleHttpRequestMessage(new Headers(expected));

		doReturn(modified).when(source).replace(expected);

		writer.write("MultipartFormMessageWriterTest", source);

		verify(source).replace(expected);

		assertEquals(expected, modified.headers().get(CONTENT_TYPE).get());
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
