/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.client.jdk;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import com.github.ljtfreitas.restify.http.client.HttpClientException;
import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.Timeout;

public class JdkHttpClientRequestFactory implements HttpClientRequestFactory {

	private final HttpClientRequestConfiguration httpClientRequestConfiguration;
	private final Charset charset;

	public JdkHttpClientRequestFactory() {
		this(Encoding.UTF_8.charset(), HttpClientRequestConfiguration.useDefault());
	}

	public JdkHttpClientRequestFactory(Charset charset) {
		this(charset, HttpClientRequestConfiguration.useDefault());
	}

	public JdkHttpClientRequestFactory(HttpClientRequestConfiguration httpClientRequestConfiguration) {
		this(httpClientRequestConfiguration.charset(), httpClientRequestConfiguration);
	}

	public JdkHttpClientRequestFactory(Charset charset, HttpClientRequestConfiguration httpClientRequestConfiguration) {
		this.charset = charset;
		this.httpClientRequestConfiguration = httpClientRequestConfiguration;
	}

	@Override
	public JdkHttpClientRequest createOf(EndpointRequest request) {
		try {
			HttpURLConnection connection = open(request.endpoint().toURL());

			configure(request, connection);

			return new JdkHttpClientRequest(connection, charset, request.headers());

		} catch (IOException e) {
			throw new HttpClientException(e);
		}
	}

	private HttpURLConnection open(URL url) throws IOException {
		Proxy proxy = httpClientRequestConfiguration.proxy().orElse(null);

		URLConnection connection = proxy == null ? url.openConnection() : url.openConnection(proxy);

		return (HttpURLConnection) connection;
	}

	private void configure(EndpointRequest source, HttpURLConnection connection) throws IOException {
		connection.setConnectTimeout(httpClientRequestConfiguration.connectionTimeout());
		connection.setReadTimeout(httpClientRequestConfiguration.readTimeout());
		connection.setInstanceFollowRedirects(httpClientRequestConfiguration.followRedirects());
		connection.setUseCaches(httpClientRequestConfiguration.useCaches());

		connection.setDoOutput(true);
		connection.setAllowUserInteraction(false);
		connection.setRequestMethod(source.method());

		if (connection instanceof HttpsURLConnection) {
			HttpsURLConnection https = (HttpsURLConnection) connection;

			httpClientRequestConfiguration.ssl().sslSocketFactory()
				.ifPresent(sslSocketFactory -> https.setSSLSocketFactory(sslSocketFactory));

			httpClientRequestConfiguration.ssl().hostnameVerifier()
				.ifPresent(hostnameVerifier -> https.setHostnameVerifier(hostnameVerifier));
		}

		source.metadata().get(Timeout.class).ifPresent(timeout -> {
			connection.setConnectTimeout((int) (timeout.connection() <= 0 ? 0 : timeout.connection()));
			connection.setReadTimeout((int) (timeout.read() <= 0 ? 0 : timeout.read()));
		});
	}
}
