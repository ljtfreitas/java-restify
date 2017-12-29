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
package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import static com.github.ljtfreitas.restify.http.client.message.Headers.AUTHORIZATION;
import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.message.Headers;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedParametersMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.json.JsonMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.converter.xml.XmlMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.DefaultEndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestWriter;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.authentication.BasicAuthentication;
import com.github.ljtfreitas.restify.http.client.jdk.HttpClientRequestConfiguration;
import com.github.ljtfreitas.restify.http.client.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.contract.Parameters.Parameter;
import com.github.ljtfreitas.restify.spi.Provider;

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
	public EndpointResponse<String> authorize(AuthorizationCodeRequest request) {
		nonNull(request.credentials().clientId(), "Your Client ID is required.");
		nonNull(request.authorizationUri(), "The authorization URI of authorization server is required.");
		nonNull(request.responseType(), "The response_type parameter is required.");

		Parameters parameters = Parameters.of(parametersOf(request));

		Headers headers = new Headers(headersOf(request));

		if (request.cookie().isPresent()) {
			headers.add("Cookie", request.cookie().get());
		}

		URI authorizationUri = URI.create(request.authorizationUri().toString() + "?" + parameters.queryString());

		return delegate.execute(new EndpointRequest(authorizationUri, "GET", headers, String.class));
	}

	private Collection<Parameter> parametersOf(AuthorizationCodeRequest request) {
		Collection<Parameter> parameters = new ArrayList<>();

		parameters.add(Parameter.of("response_type", request.responseType()));
		parameters.add(Parameter.of("client_id", request.credentials().clientId()));
		parameters.add(Parameter.of("scope", request.scope()));

		if (request.redirectUri().isPresent()) {
			parameters.add(Parameter.of("redirect_uri", request.redirectUri().get().toString()));
		}

		if (request.state().isPresent()) {
			parameters.add(Parameter.of("state", request.state().get()));
		}

		return parameters;
	}

	private Collection<Header> headersOf(AuthorizationCodeRequest request) {
		Collection<Header> headers = new ArrayList<>(request.headers().all());

		if (request.cookie().isPresent()) {
			headers.add(Header.cookie(request.cookie().get()));
		}

		return headers;
	}

	@Override
	public EndpointResponse<AccessToken> requireToken(AccessTokenRequest request) {
		EndpointRequest accessTokenEndpointRequest = new AccessTokenEndpointRequestFactory(request).create();

		EndpointResponse<AccessTokenResponse> accessTokenResponse = delegate.execute(accessTokenEndpointRequest);

		return buildAccessToken(accessTokenResponse);
	}

	private EndpointResponse<AccessToken> buildAccessToken(EndpointResponse<AccessTokenResponse> accessTokenResponse) {
		AccessToken accessToken = AccessToken.of(accessTokenResponse.body());

		return new EndpointResponse<>(accessTokenResponse.status(), accessTokenResponse.headers(), accessToken);
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
			return new DefaultEndpointRequestExecutor(httpClientRequestFactory, endpointRequestWriter(converters),
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
			Collection<HttpMessageConverter> converters = new ArrayList<>();
			
			converters.add(new TextPlainMessageConverter());
			converters.add(new FormURLEncodedParametersMessageConverter());
			
			Provider provider = new Provider();
			provider.single(JsonMessageConverter.class).ifPresent(converters::add);
			provider.single(XmlMessageConverter.class).ifPresent(converters::add);

			return new HttpMessageConverters(converters);
		}
	}

	private class AccessTokenEndpointRequestFactory {

		private final AccessTokenRequest source;

		private AccessTokenEndpointRequestFactory(AccessTokenRequest source) {
			this.source = source;
		}

		private EndpointRequest create() {
			Headers headers = new Headers(Header.contentType(FORM_URLENCODED_CONTENT_TYPE))
					.addAll(source.headers());

			Parameters body = source.parameters();

			return authenticated(new EndpointRequest(source.uri(), "POST", headers, body, AccessTokenResponse.class));
		}

		private EndpointRequest authenticated(EndpointRequest endpointRequest) {
			switch (clientAuthenticationMethod) {
				case HEADER:
					return endpointRequest.add(authorization());

				case FORM_PARAMETER:
					Parameters body = ((Parameters) endpointRequest.body().get());
					return endpointRequest.usingBody(body.putAll(parameters()));

				case QUERY_PARAMETER:
					return endpointRequest.append(parameters());
			}

			return endpointRequest;
		}

		private Parameters parameters() {
			Parameters parameters = new Parameters();

			ClientCredentials credentials = source.credentials();
			parameters = parameters.put("client_id", credentials.clientId());

			if (credentials.clientSecret() != null && !"".equals(credentials.clientSecret())) {
				parameters = parameters.put("client_secret", credentials.clientSecret());
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
