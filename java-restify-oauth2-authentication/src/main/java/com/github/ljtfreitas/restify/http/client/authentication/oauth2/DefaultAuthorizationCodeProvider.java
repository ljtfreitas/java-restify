package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static com.github.ljtfreitas.restify.http.util.Preconditions.nonNull;

import java.net.URI;
import java.util.Arrays;

import com.github.ljtfreitas.restify.http.client.Header;
import com.github.ljtfreitas.restify.http.client.message.HttpMessageConverters;
import com.github.ljtfreitas.restify.http.client.message.converter.text.TextPlainMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequest;
import com.github.ljtfreitas.restify.http.client.request.HttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.request.jdk.HttpClientRequestConfiguration;
import com.github.ljtfreitas.restify.http.client.request.jdk.JdkHttpClientRequestFactory;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponseReader;
import com.github.ljtfreitas.restify.http.client.response.StatusCode;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.contract.metadata.reflection.JavaType;

public class DefaultAuthorizationCodeProvider implements OAuth2AuthorizationCodeProvider {

	private final OAuth2AuthorizationConfiguration configuration;
	private final HttpClientRequestFactory httpClientRequestFactory;
	private final EndpointResponseReader endpointResponseReader;

	public DefaultAuthorizationCodeProvider(OAuth2AuthorizationConfiguration configuration) {
		this.configuration = configuration;
		this.httpClientRequestFactory = httpClientRequestFactory();
		this.endpointResponseReader = endpointResponseReader();
	}

	private EndpointResponseReader endpointResponseReader() {
		HttpMessageConverters converters = new HttpMessageConverters(Arrays.asList(new TextPlainMessageConverter()));

		return new EndpointResponseReader(converters, new OAuth2EndpointResponseErrorFallback());
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
	public String get() {
		HttpClientRequest authorizationRequest = httpClientRequestFactory.createOf(buildAuthorizationRequest());

		EndpointResponse<String> authorizationResponse = endpointResponseReader.read(authorizationRequest.execute(),
				JavaType.of(String.class));

		StatusCode status = authorizationResponse.code();

		if (status.isOK()) {
			String message = "Do you approve the client [" + configuration.credentials().clientId() + "] to access your resources "
				+ "with scopes [" + configuration.scopes() + "]";

			throw new OAuth2UserApprovalRequiredException(message);

		} else {
			Header location = authorizationResponse.headers().get("Location")
					.orElseThrow(() -> new IllegalStateException("Location header must be present on Authorization redirect!"));

			Parameters parameters = Parameters.from(URI.create(location.value()));

			return parameters.get("code")
					.orElseThrow(() -> new IllegalStateException("Authorization code parameter must be present on Authorization redirect!"));
		}
	}

	private EndpointRequest buildAuthorizationRequest() {
		nonNull(configuration.credentials().clientId(), "Your Client ID is required.");
		nonNull(configuration.authorizationUri(), "The Authorization URI of authorization server is required.");

		Parameters parameters = new Parameters();
		parameters.put("response_type", "code");
		parameters.put("client_id", configuration.credentials().clientId());
		parameters.put("redirect_uri", configuration.redirectUri().toString());
		parameters.put("scope", configuration.scope());

		URI authorizationUri = URI.create(configuration.authorizationUri().toString() + "?" + parameters.queryString());

		return new EndpointRequest(authorizationUri, "GET");
	}
}
