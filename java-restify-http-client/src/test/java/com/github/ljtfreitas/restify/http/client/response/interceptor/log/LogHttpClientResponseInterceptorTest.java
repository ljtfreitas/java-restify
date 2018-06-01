package com.github.ljtfreitas.restify.http.client.response.interceptor.log;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
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
import com.github.ljtfreitas.restify.http.client.message.response.BufferedHttpResponseBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseBody;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;

@RunWith(MockitoJUnitRunner.class)
public class LogHttpClientResponseInterceptorTest {

	@Mock
	private HttpClientResponse response;

	private MyHandler handler;

	private LogHttpClientResponseInterceptor interceptor;

	@Before
	public void setup() {
		interceptor = new LogHttpClientResponseInterceptor();

		when(response.status())
			.thenReturn(StatusCode.ok());
		when(response.headers())
			.thenReturn(new Headers(Header.date(Instant.now()), Header.contentType("text/plain"), Header.of("Cache-Control", "private, max-age=0")));

		handler = new MyHandler();

		Logger.getLogger(LogHttpClientResponseInterceptor.class.getCanonicalName()).addHandler(handler);
	}

	@Test
	public void shouldLogHttpResponseWithoutBody() {
		HttpClientResponse newResponse = interceptor.intercepts(response);

		assertSame(response, newResponse);

		LogRecord record = handler.entries.poll();

		String message = record.getMessage();

		assertThat(message, containsString("< 200 Ok"));
		assertThat(message, containsString("< Date: "));
		assertThat(message, containsString("< Content-Type: text/plain"));
		assertThat(message, containsString("< Cache-Control: private, max-age=0"));
	}

	@Test
	public void shouldLogHttpResponseWithBody() throws IOException {
		HttpResponseBody body = BufferedHttpResponseBody
				.of(new ByteArrayInputStream("This is a message body".getBytes()));

		when(response.body())
			.thenReturn(body);

		when(response.available())
			.thenReturn(true);

		HttpClientResponse newResponse = interceptor.intercepts(response);

		assertSame(response, newResponse);

		LogRecord record = handler.entries.poll();

		String message = record.getMessage();

		assertThat(message, containsString("< 200 Ok"));
		assertThat(message, containsString("< Date: "));
		assertThat(message, containsString("< Content-Type: text/plain"));
		assertThat(message, containsString("< Cache-Control: private, max-age=0"));
		assertThat(message, containsString("< This is a message body"));
	}

	private class MyHandler extends Handler {

		private final Queue<LogRecord> entries = new LinkedList<>();

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
