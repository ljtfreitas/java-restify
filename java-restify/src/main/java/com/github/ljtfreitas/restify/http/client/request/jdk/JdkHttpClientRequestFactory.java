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
package com.github.ljtfreitas.restify.http.client.request.jdk;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import com.github.ljtfreitas.restify.http.RestifyHttpException;
import com.github.ljtfreitas.restify.http.client.charset.Encoding;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;

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
			HttpURLConnection connection = (HttpURLConnection) request.endpoint().toURL().openConnection();

			connection.setConnectTimeout(httpClientRequestConfiguration.connectionTimeout());
			connection.setReadTimeout(httpClientRequestConfiguration.readTimeout());
			connection.setReadTimeout(httpClientRequestConfiguration.readTimeout());
			connection.setInstanceFollowRedirects(httpClientRequestConfiguration.followRedirects());
			connection.setUseCaches(httpClientRequestConfiguration.useCaches());

			connection.setDoOutput(true);
			connection.setAllowUserInteraction(false);
			connection.setRequestMethod(request.method());

			if (connection instanceof HttpsURLConnection) {
				HttpsURLConnection https = (HttpsURLConnection) connection;

				httpClientRequestConfiguration.sslSocketFactory()
						.ifPresent(sslSocketFactory -> https.setSSLSocketFactory(sslSocketFactory));

				httpClientRequestConfiguration.hostnameVerifier()
						.ifPresent(hostnameVerifier -> https.setHostnameVerifier(hostnameVerifier));
			}

			return new JdkHttpClientRequest(connection, charset, request.headers());

		} catch (IOException e) {
			throw new RestifyHttpException(e);
		}
	}
}
