package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;

@RunWith(MockitoJUnitRunner.class)
public class BaseMultipartFormMessageWriterTest {

	@Mock
	private HttpRequestMessage request;

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

		when(request.headers()).thenReturn(new Headers(Header.contentType("multipart/form-data")));
		when(request.replace(notNull(Header.class))).thenReturn(request);
	}

	@Test
	public void shouldAddBoundaryParameterToContentTypeHeader() {
		String expectedContentType = "multipart/form-data; boundary=----abc1234";
		
		Header expectedHeader = Header.contentType("multipart/form-data; boundary=----abc1234");

		Headers headers = new Headers(Header.contentType("multipart/form-data"));

		when(request.headers()).thenReturn(headers);
		
		HttpRequestMessage newRequest = mock(HttpRequestMessage.class);
		when(newRequest.output()).thenReturn(new ByteArrayOutputStream());

		Answer<HttpRequestMessage> answer = new Answer<HttpRequestMessage>() {
			@Override
			public HttpRequestMessage answer(InvocationOnMock invocation) throws Throwable {
				Header header = invocation.getArgumentAt(0, Header.class);

				Headers newHeaders = ((HttpRequestMessage) invocation.getMock()).headers().replace(header);
				when(newRequest.headers()).thenReturn(newHeaders);

				return newRequest;
			}
		};

		doAnswer(answer).when(request).replace(expectedHeader);

		writer.write("MultipartFormMessageWriterTest", request);

		doReturn(newRequest).when(request).replace(expectedHeader);

		assertEquals(expectedContentType, newRequest.headers().get("Content-Type").map(Header::value).get());
	}

	@Test
	public void shouldWriteHttpRequestBody() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		when(request.output()).thenReturn(output);

		writer.write("MultipartFormMessageWriterTest", request);

		String expectedBody = "MultipartFormMessageWriterTest\r\n------abc1234--";

		assertEquals(expectedBody, output.toString());
	}
}
