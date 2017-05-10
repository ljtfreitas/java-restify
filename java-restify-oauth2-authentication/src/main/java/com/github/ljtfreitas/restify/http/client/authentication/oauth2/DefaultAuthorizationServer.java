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
package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static com.github.ljtfreitas.restify.http.client.Headers.AUTHORIZATION;
import static com.github.ljtfreitas.restify.http.client.Headers.CONTENT_TYPE;
import static com.github.ljtfreitas.restify.http.util.Preconditions.nonNull;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.Headers;
import com.github.ljtfreitas.restify.http.client.authentication.BasicAuthentication;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.form.FormURLEncodedParametersMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.RestifyEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.jdk.HttpClientRequestConfiguration;
import com.github.ljtfreitas.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.contract.Parameters;

class DefaultAuthorizationServer implements AuthorizationServer {

	private static final String FORM_URLENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";

	private final EndpointRequestExecutor delegate;

	public DefaultAuthorizationServer(EndpointRequestExecutor endpointRequestExecutor) {
		this.delegate = endpointRequestExecutor;
	}

	public DefaultAuthorizationServer(HttpMessageConverters converters) {
		this.delegate = new RestifyEndpointRequestExecutor(httpClientRequestFactory(), endpointRequestWriter(converters),
				endpointResponseReader(converters));
	}

	public DefaultAuthorizationServer(HttpClientRequestFactory httpClientRequestFactory) {
		HttpMessageConverters converters = converters();
		this.delegate = new RestifyEndpointRequestExecutor(httpClientRequestFactory, endpointRequestWriter(converters),
				endpointResponseReader(converters));
	}

	public DefaultAuthorizationServer() {
		HttpMessageConverters converters = converters();
		this.delegate = new RestifyEndpointRequestExecutor(httpClientRequestFactory(), endpointRequestWriter(converters),
				endpointResponseReader(converters));
	}

	private HttpMessageConverters converters() {
		List<HttpMessageConverter> converters = Arrays.asList(new TextPlainMessageConverter(),
				new FormURLEncodedParametersMessageConverter(), JsonMessageConverter.available());
		return new HttpMessageConverters(converters);
	}

	private EndpointRequestWriter endpointRequestWriter(HttpMessageConverters converters) {
		return new EndpointRequestWriter(converters);
	}

	private EndpointResponseReader endpointResponseReader(HttpMessageConverters converters) {
		return new EndpointResponseReader(converters, new AuthorizationServerResponseErrorFallback());
	}

	private HttpClientRequestFactory httpClientRequestFactory() {
		HttpClientRequestConfiguration httpClientRequestConfiguration = new HttpClientRequestConfiguration.Builder()
				.followRedirects()
					.disabled()
				.useCaches()
					.disabled()
				.build();

		return new JdkHttpClientRequestFactory(httpClientRequestConfiguration);
	}

	@Override
	public EndpointResponse<String> authorize(OAuth2AuthorizationConfiguration configuration) {
		nonNull(configuration.credentials().clientId(), "Your Client ID is required.");
		nonNull(configuration.authorizationUri(), "The authorization URI of authorization server is required.");
		nonNull(configuration.responseType(), "The response_type parameter is required.");

		Parameters parameters = new Parameters();
		parameters.put("response_type", configuration.responseType());
		parameters.put("client_id", configuration.credentials().clientId());
		parameters.put("scope", configuration.scope());

		if (configuration.redirectUri().isPresent()) {
			parameters.put("redirect_uri", configuration.redirectUri().get().toString());
		}

		if (configuration.state().isPresent()) {
			parameters.put("state", configuration.state().get());
		}

		Headers headers = new Headers();

		if (configuration.cookie().isPresent()) {
			headers.put("Cookie", configuration.cookie().get());
		}

		URI authorizationUri = URI.create(configuration.authorizationUri().toString() + "?" + parameters.queryString());

		return delegate.execute(new EndpointRequest(authorizationUri, "GET", headers, String.class));
	}

	@Override
	public EndpointResponse<AccessToken> requireToken(AccessTokenRequest request) {
		Header contentType = new Header(CONTENT_TYPE, FORM_URLENCODED_CONTENT_TYPE);

		Headers headers = new Headers(contentType, authorization(request.credentials()));
		headers.putAll(request.headers());

		Parameters body = request.parameters();

		EndpointResponse<Map<String, Object>> accessTokenResponse = delegate
				.execute(new EndpointRequest(request.uri(), "POST", headers, body, Map.class));

		return buildAccessToken(accessTokenResponse);
	}

	private EndpointResponse<AccessToken> buildAccessToken(EndpointResponse<Map<String, Object>> accessTokenResponse) {
		Parameters parameters = Parameters.of(accessTokenResponse.body());

		AccessToken accessToken = AccessToken.create(parameters);

		return new EndpointResponse<>(accessTokenResponse.code(), accessTokenResponse.headers(), accessToken);
	}

	private Header authorization(ClientCredentials credentials) {
		BasicAuthentication basic = new BasicAuthentication(credentials.clientId(), credentials.clientSecret());
		return new Header(AUTHORIZATION, basic.content());
	}
}
