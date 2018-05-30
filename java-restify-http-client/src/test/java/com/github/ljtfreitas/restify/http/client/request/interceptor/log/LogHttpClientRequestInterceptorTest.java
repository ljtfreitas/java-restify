package com.github.ljtfreitas.restify.http.client.request.interceptor.log;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
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
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;

@RunWith(MockitoJUnitRunner.class)
public class LogHttpClientRequestInterceptorTest {

	@Mock
	private HttpClientRequest request;
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
			.thenReturn(new RequestBody());
		when(request.charset())
			.thenReturn(Charset.forName("UTF-8"));

		handler = new MyHandler();

		Logger.getLogger(LogHttpClientRequestInterceptor.class.getCanonicalName()).addHandler(handler);

	}

	@Test
	public void shouldLogHttpRequest() {
		HttpClientRequest log = interceptor.intercepts(request);

		log.execute();

		LogRecord record = handler.entries.poll();

		String message = record.getMessage();

		assertThat(message, containsString("> GET http://my.api.com/path"));
		assertThat(message, containsString("> Host: http://my.api.com"));
		assertThat(message, containsString("> User-Agent: http-java-restify-2.0"));
		assertThat(message, containsString("> Date: "));
	}

	@Test
	public void shouldLogHttpRequestWithBody() throws IOException {
		RequestBody body = new RequestBody();
		body.write("This is a message body".getBytes());
		body.flush();

		when(request.body())
			.thenReturn(body);

		HttpClientRequest log = interceptor.intercepts(request);

		log.execute();

		LogRecord record = handler.entries.poll();

		String message = record.getMessage();

		assertThat(message, containsString("> GET http://my.api.com/path"));
		assertThat(message, containsString("> Host: http://my.api.com"));
		assertThat(message, containsString("> User-Agent: http-java-restify-2.0"));
		assertThat(message, containsString("> Date: "));
		assertThat(message, containsString("> This is a message body"));
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
