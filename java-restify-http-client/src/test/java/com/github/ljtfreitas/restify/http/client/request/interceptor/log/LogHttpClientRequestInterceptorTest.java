package com.github.ljtfreitas.restify.http.client.request.interceptor.log;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.io.InputStreamContent;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedByteArrayHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseBody;
import com.github.ljtfreitas.restify.http.client.message.response.InputStreamHttpResponseBody;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;

@RunWith(MockitoJUnitRunner.class)
public class LogHttpClientRequestInterceptorTest {

	@Mock
	private HttpClientRequest request;

	@Mock
	private HttpClientResponse response;

	private MyHandler handler;

	private LogHttpClientRequestInterceptor interceptor;

	@Before
	public void setup() {
		interceptor = new LogHttpClientRequestInterceptor();

		when(request.method())
			.thenReturn("GET");
		when(request.uri())
			.thenReturn(URI.create("http://my.api.com/path"));
		when(request.headers())
			.thenReturn(new Headers(Header.host("http://my.api.com"), Header.userAgent("http-java-restify-2.0"), Header.date(Instant.now())));
		when(request.body())
			.thenReturn(new BufferedByteArrayHttpRequestBody());
		when(request.charset())
			.thenReturn(Charset.forName("UTF-8"));
		when(request.execute())
			.thenReturn(response);

		when(response.status())
			.thenReturn(StatusCode.ok());
		when(response.headers())
			.thenReturn(new Headers(Header.date(Instant.now()), Header.contentType("text/plain"), Header.of("Cache-Control", "private, max-age=0")));
		when(response.body())
			.thenReturn(InputStreamHttpResponseBody.empty());

		handler = new MyHandler();

		Logger.getLogger(LoggableHttpClientRequest.class.getCanonicalName()).addHandler(handler);
	}

	@Test
	public void shouldLogHttpRequestWithoutBody() {
		HttpClientRequest loggableHttpClientRequest = interceptor.intercepts(request);

		loggableHttpClientRequest.execute();

		LogRecord record = handler.entries.poll();

		String message = record.getMessage();

		assertThat(message, containsString("GET http://my.api.com/path"));
		assertThat(message, containsString("Host: http://my.api.com"));
		assertThat(message, containsString("User-Agent: http-java-restify-2.0"));
		assertThat(message, containsString("Date: "));
	}

	@Test
	public void shouldLogHttpResponseWithoutBody() {
		HttpClientRequest loggableHttpClientRequest = interceptor.intercepts(request);

		loggableHttpClientRequest.execute();

		LogRecord record = handler.entries.pollLast();

		String message = record.getMessage();

		assertThat(message, containsString("200 Ok"));
		assertThat(message, containsString("Date: "));
		assertThat(message, containsString("Content-Type: text/plain"));
		assertThat(message, containsString("Cache-Control: private, max-age=0"));
	}

	@Test
	public void shouldLogHttpRequestWithBody() throws IOException {
		HttpClientRequest loggableHttpClientRequest = interceptor.intercepts(request);

		HttpRequestBody body = loggableHttpClientRequest.body();
		body.output().write("This is a message body".getBytes());
		body.output().flush();

		loggableHttpClientRequest.execute();

		LogRecord record = handler.entries.poll();

		String message = record.getMessage();

		assertThat(message, containsString("GET http://my.api.com/path"));
		assertThat(message, containsString("Host: http://my.api.com"));
		assertThat(message, containsString("User-Agent: http-java-restify-2.0"));
		assertThat(message, containsString("Date: "));
		assertThat(message, containsString("This is a message body"));
	}

	@Test
	public void shouldLogHttpResponseWithBody() throws IOException {
		HttpResponseBody body = new InputStreamHttpResponseBody(new ByteArrayInputStream("This is a message body".getBytes()));

		when(response.body())
			.thenReturn(body);

		when(response.available())
			.thenReturn(true);

		HttpClientRequest loggableHttpClientRequest = interceptor.intercepts(request);

		HttpClientResponse loggableHttpClientResponse = loggableHttpClientRequest.execute();

		LogRecord record = handler.entries.pollLast();

		String message = record.getMessage();

		assertThat(message, containsString("200 Ok"));
		assertThat(message, containsString("Date: "));
		assertThat(message, containsString("Content-Type: text/plain"));
		assertThat(message, containsString("Cache-Control: private, max-age=0"));
		assertThat(message, containsString("This is a message body"));

		String responseAsString = new InputStreamContent(loggableHttpClientResponse.body().input()).asString();
		assertEquals("This is a message body", responseAsString);
	}

	private class MyHandler extends Handler {

		private final Deque<LogRecord> entries = new LinkedList<>();

		@Override
		public void publish(LogRecord record) {
			entries.add(record);
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}
	}
}
