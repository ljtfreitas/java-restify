package com.github.ljtfreitas.restify.http.client.request.interceptor.gzip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.io.InputStreamContent;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedByteArrayHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.ByteArrayHttpResponseBody;
import com.github.ljtfreitas.restify.http.client.message.response.InputStreamHttpResponseBody;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.response.HttpClientResponse;
import com.github.ljtfreitas.restify.util.Try;

@RunWith(MockitoJUnitRunner.class)
public class GzipHttpClientRequestInterceptorTest {

	@Mock
	private HttpClientRequest request;

	@Mock
	private HttpClientResponse response;

	private GzipHttpClientRequestInterceptor interceptor;

	@Before
	public void setup() throws Exception {
		interceptor = new GzipHttpClientRequestInterceptor();

		when(response.body())
			.thenReturn(InputStreamHttpResponseBody.empty());

		when(response.headers())
			.thenReturn(new Headers(Header.contentEncoding("gzip")));

		when(request.execute())
			.thenReturn(response);
	}

	@Test
	public void shouldReadGzippedResponseBody() throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		Try.withResources(() -> new GZIPOutputStream(output))
			.apply(gzipContentOutput -> gzipContentOutput.write("http response".getBytes()))
			.apply(gzipContentOutput -> gzipContentOutput.flush());

		when(response.body())
			.thenReturn(new InputStreamHttpResponseBody(new ByteArrayInputStream(output.toByteArray())));

		HttpClientRequest gzipHttpClientRequest = interceptor.intercepts(request);

		HttpClientResponse gzipHttpClientResponse = gzipHttpClientRequest.execute();

		assertTrue(gzipHttpClientResponse.body().input() instanceof GZIPInputStream);

		String responseAsString = ByteArrayHttpResponseBody.of(gzipHttpClientResponse.body()).asString();

		assertEquals("http response", responseAsString);
	}

	@Test
	public void shouldNotUseGzipResponseWhenContentEncodingHeaderNotContainsGzip() {
		when(response.body())
			.thenReturn(new InputStreamHttpResponseBody(new ByteArrayInputStream("simple http response".getBytes())));
		when(response.headers())
			.thenReturn(Headers.empty());

		HttpClientRequest httpClientRequest = interceptor.intercepts(request);

		HttpClientResponse httpClientResponse = httpClientRequest.execute();

		assertSame(response, httpClientResponse);

		assertFalse(httpClientResponse.body().input() instanceof GZIPInputStream);

		String responseAsString = ByteArrayHttpResponseBody.of(httpClientResponse.body()).asString();

		assertEquals("simple http response", responseAsString);
	}

	@Test
	public void shouldNotUseGzipResponseWhenContentEncodingHeaderUseOtherAlgorithm() {
		when(response.body())
			.thenReturn(new InputStreamHttpResponseBody(new ByteArrayInputStream("simple http response".getBytes())));
		when(response.headers())
			.thenReturn(new Headers(Header.contentEncoding("br")));

		HttpClientRequest httpClientRequest = interceptor.intercepts(request);

		HttpClientResponse httpClientResponse = httpClientRequest.execute();

		assertSame(response, httpClientResponse);

		assertFalse(httpClientResponse.body().input() instanceof GZIPInputStream);

		String responseAsString = ByteArrayHttpResponseBody.of(httpClientResponse.body()).asString();

		assertEquals("simple http response", responseAsString);
	}

	@Test
	public void shouldNotUseGzipResponseWhenDisabled() {
		interceptor = new GzipHttpClientRequestInterceptor.Builder()
				.encoding()
					.response(false)
					.build();

		when(response.body())
			.thenReturn(new InputStreamHttpResponseBody(new ByteArrayInputStream("simple http response".getBytes())));

		HttpClientRequest httpClientRequest = interceptor.intercepts(request);

		HttpClientResponse httpClientResponse = httpClientRequest.execute();

		assertSame(response, httpClientResponse);

		assertFalse(httpClientResponse.body().input() instanceof GZIPInputStream);

		String responseAsString = ByteArrayHttpResponseBody.of(httpClientResponse.body()).asString();

		assertEquals("simple http response", responseAsString);
	}

	@Test
	public void shouldWriteGzippedRequestBody() throws Exception {
		interceptor = new GzipHttpClientRequestInterceptor.Builder()
				.encoding()
					.request()
					.response(false)
						.build();

		when(request.headers())
			.thenReturn(Headers.empty());

		when(request.replace(Header.contentEncoding("gzip")))
			.thenReturn(request);

		BufferedByteArrayHttpRequestBody body = new BufferedByteArrayHttpRequestBody();

		when(request.body())
			.thenReturn(body);

		HttpClientRequest gzipHttpClientRequest = interceptor.intercepts(request);

		OutputStream outputBody = gzipHttpClientRequest.body().output();

		assertTrue(outputBody instanceof GZIPOutputStream);

		outputBody.write("http request".getBytes());
		outputBody.flush();
		outputBody.close();

		HttpClientResponse response = gzipHttpClientRequest.execute();
		assertNotNull(response);

		verify(request).replace(Header.contentEncoding("gzip"));

		InputStreamContent result = new InputStreamContent(new GZIPInputStream(new ByteArrayInputStream(body.asBytes())));

		assertEquals("http request", result.asString());
	}

	@Test
	public void shouldNotWriteGzipRequestBodyWhenDisabled() throws Exception {
		interceptor = new GzipHttpClientRequestInterceptor.Builder()
				.encoding()
					.request(false)
					.response(false)
						.build();

		when(request.headers())
			.thenReturn(Headers.empty());

		BufferedByteArrayHttpRequestBody body = new BufferedByteArrayHttpRequestBody();

		when(request.body())
			.thenReturn(body);

		HttpClientRequest httpClientRequest = interceptor.intercepts(request);

		OutputStream outputBody = httpClientRequest.body().output();

		assertFalse(outputBody instanceof GZIPOutputStream);

		outputBody.write("http request".getBytes());
		outputBody.flush();
		outputBody.close();

		HttpClientResponse httpClientResponse = httpClientRequest.execute();

		assertNotNull(httpClientResponse);

		assertFalse(httpClientResponse.body().input() instanceof GZIPInputStream);

		assertEquals("http request", new String(body.asBytes()));
	}
}
