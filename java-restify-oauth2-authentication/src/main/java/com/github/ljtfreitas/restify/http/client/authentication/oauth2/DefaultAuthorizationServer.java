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

import static com.github.ljtfreitas.restify.http.client.header.Headers.AUTHORIZATION;
import static com.github.ljtfreitas.restify.http.util.Preconditions.nonNull;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import com.github.ljtfreitas.restify.http.client.authentication.BasicAuthentication;
import com.github.ljtfreitas.restify.http.client.header.Header;
import com.github.ljtfreitas.restify.http.client.header.Headers;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.JaxbXmlMessageConverter;
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

public class DefaultAuthorizationServer implements AuthorizationServer {

	private static final String FORM_URLENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";

	private final EndpointRequestExecutor delegate;
	private final ClientAuthenticationMethod clientAuthenticationMethod;

	public DefaultAuthorizationServer() {
		this(new EndpointRequestExecutorFactory().create());
	}

	public DefaultAuthorizationServer(EndpointRequestExecutor endpointRequestExecutor) {
		this(endpointRequestExecutor, ClientAuthenticationMethod.HEADER);
	}

	public DefaultAuthorizationServer(ClientAuthenticationMethod clientAuthenticationMethod) {
		this(new EndpointRequestExecutorFactory().create(), clientAuthenticationMethod);
	}

	public DefaultAuthorizationServer(HttpMessageConverters converters) {
		this(new EndpointRequestExecutorFactory(converters).create());
	}

	public DefaultAuthorizationServer(HttpMessageConverters converters, ClientAuthenticationMethod clientAuthenticationMethod) {
		this(new EndpointRequestExecutorFactory(converters).create(), clientAuthenticationMethod);
	}

	public DefaultAuthorizationServer(HttpClientRequestFactory httpClientRequestFactory) {
		this(new EndpointRequestExecutorFactory(httpClientRequestFactory).create());
	}

	public DefaultAuthorizationServer(HttpClientRequestFactory httpClientRequestFactory, ClientAuthenticationMethod clientAuthenticationMethod) {
		this(new EndpointRequestExecutorFactory(httpClientRequestFactory).create(), clientAuthenticationMethod);
	}

	public DefaultAuthorizationServer(EndpointRequestExecutor endpointRequestExecutor, ClientAuthenticationMethod clientAuthenticationMethod) {
		this.delegate = endpointRequestExecutor;
		this.clientAuthenticationMethod = clientAuthenticationMethod;
	}

	@Override
	public EndpointResponse<String> authorize(AuthorizationCodeGrantProperties properties) {
		nonNull(properties.credentials().clientId(), "Your Client ID is required.");
		nonNull(properties.authorizationUri(), "The authorization URI of authorization server is required.");
		nonNull(properties.responseType(), "The response_type parameter is required.");

		Parameters parameters = new Parameters();
		parameters.put("response_type", properties.responseType());
		parameters.put("client_id", properties.credentials().clientId());
		parameters.put("scope", properties.scope());

		if (properties.redirectUri().isPresent()) {
			parameters.put("redirect_uri", properties.redirectUri().get().toString());
		}

		if (properties.state().isPresent()) {
			parameters.put("state", properties.state().get());
		}

		Headers headers = new Headers(properties.headers());

		if (properties.cookie().isPresent()) {
			headers.add("Cookie", properties.cookie().get());
		}

		URI authorizationUri = URI.create(properties.authorizationUri().toString() + "?" + parameters.queryString());

		return delegate.execute(new EndpointRequest(authorizationUri, "GET", headers, String.class));
	}

	@Override
	public EndpointResponse<AccessToken> requireToken(AccessTokenRequest request) {
		EndpointRequest accessTokenEndpointRequest = new AccessTokenEndpointRequestFactory(request).create();

		EndpointResponse<AccessTokenResponse> accessTokenResponse = delegate.execute(accessTokenEndpointRequest);

		return buildAccessToken(accessTokenResponse);
	}

	private EndpointResponse<AccessToken> buildAccessToken(EndpointResponse<AccessTokenResponse> accessTokenResponse) {
		AccessToken accessToken = AccessToken.of(accessTokenResponse.body());

		return new EndpointResponse<>(accessTokenResponse.code(), accessTokenResponse.headers(), accessToken);
	}

	private static class EndpointRequestExecutorFactory {

		private final HttpMessageConverters converters;
		private final HttpClientRequestFactory httpClientRequestFactory;

		private EndpointRequestExecutorFactory() {
			this.httpClientRequestFactory = httpClientRequestFactory();
			this.converters = converters();
		}

		private EndpointRequestExecutorFactory(HttpClientRequestFactory httpClientRequestFactory) {
			this.httpClientRequestFactory = httpClientRequestFactory;
			this.converters = converters();
		}

		private EndpointRequestExecutorFactory(HttpMessageConverters converters) {
			this.converters = converters;
			this.httpClientRequestFactory = httpClientRequestFactory();
		}

		public EndpointRequestExecutor create() {
			return new RestifyEndpointRequestExecutor(httpClientRequestFactory, endpointRequestWriter(converters),
					endpointResponseReader(converters));
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

		private EndpointRequestWriter endpointRequestWriter(HttpMessageConverters converters) {
			return new EndpointRequestWriter(converters);
		}

		private EndpointResponseReader endpointResponseReader(HttpMessageConverters converters) {
			return new EndpointResponseReader(converters, new AuthorizationServerResponseErrorFallback());
		}

		private HttpMessageConverters converters() {
			List<HttpMessageConverter> converters = Arrays.asList(new TextPlainMessageConverter(),
					new FormURLEncodedParametersMessageConverter(), JsonMessageConverter.available(), new JaxbXmlMessageConverter<>());
			return new HttpMessageConverters(converters);
		}
	}

	private class AccessTokenEndpointRequestFactory {

		private final AccessTokenRequest source;

		private AccessTokenEndpointRequestFactory(AccessTokenRequest source) {
			this.source = source;
		}

		private EndpointRequest create() {
			Headers headers = new Headers(Header.contentType(FORM_URLENCODED_CONTENT_TYPE));
			headers.addAll(source.headers());

			Parameters body = source.parameters();

			return authenticated(new EndpointRequest(source.uri(), "POST", headers, body, AccessTokenResponse.class));
		}

		private EndpointRequest authenticated(EndpointRequest endpointRequest) {
			switch (clientAuthenticationMethod) {
				case HEADER:
					endpointRequest.headers().add(authorization());
					return endpointRequest;

				case FORM_PARAMETER:
					((Parameters) endpointRequest.body().get()).putAll(parameters());
					return endpointRequest;

				case QUERY_PARAMETER:
					return endpointRequest.append(parameters());
			}

			return endpointRequest;
		}

		private Parameters parameters() {
			Parameters parameters = new Parameters();

			ClientCredentials credentials = source.credentials();
			parameters.put("client_id", credentials.clientId());

			if (credentials.clientSecret() != null && !"".equals(credentials.clientSecret())) {
				parameters.put("client_secret", credentials.clientSecret());
			}

			return parameters;
		}

		private Header authorization() {
			ClientCredentials credentials = source.credentials();
			BasicAuthentication basic = new BasicAuthentication(credentials.clientId(), credentials.clientSecret());
			return new Header(AUTHORIZATION, basic.content(null));
		}
	}
}
