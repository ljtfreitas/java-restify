package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeGrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.DefaultAuthorizationCodeProvider;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2Exception;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2UserApprovalRequiredException;

public class DefaultAuthorizationCodeProviderTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8088);

	private MockServerClient mockServerClient;

	private DefaultAuthorizationCodeProvider provider;

	private OAuth2AuthenticatedEndpointRequest request;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8088);

		AuthorizationCodeGrantProperties properties = new AuthorizationCodeGrantProperties.Builder()
				.authorizationUri("http://localhost:8088/oauth/authorize")
				.clientId("client-id")
				.redirectUri("http://my.web.app/oauth/callback")
				.scopes("read", "write")
				.responseType("code")
				.state("current-state")
				.build();

		EndpointRequest source = new EndpointRequest(URI.create("http://my.resource.server/path"), "GET");

		request = new OAuth2AuthenticatedEndpointRequest(source, properties);

		provider = new DefaultAuthorizationCodeProvider();
	}

	@Test
	public void shouldGetAuthorizationCodeFromRedirectLocationUri() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/oauth/authorize")
				.withQueryStringParameters(new Parameter("response_type", "code"),
										   new Parameter("client_id", "client-id"),
										   new Parameter("redirect_uri", "http://my.web.app/oauth/callback"),
										   new Parameter("scope", "read write"),
										   new Parameter("state", "current-state")))
			.respond(response()
				.withStatusCode(302)
				.withHeader("Content-Type", "application/x-www-form-urlencoded")
				.withHeader("Location", "http://my.web.app/oauth/callback?code=abc1234&state=current-state"));

		assertEquals("abc1234", provider.provides(request));
	}

	@Test(expected = OAuth2UserApprovalRequiredException.class)
	public void shouldThrowUserApprovalRequiredExceptionWhenAuthorizationResponseIsOk() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/oauth/authorize")
				.withQueryStringParameters(new Parameter("response_type", "code"),
										   new Parameter("client_id", "client-id"),
										   new Parameter("redirect_uri", "http://my.web.app/oauth/callback"),
										   new Parameter("scope", "read write")))
			.respond(response()
				.withStatusCode(200));

		provider.provides(request);
	}

	@Test(expected = OAuth2Exception.class)
	public void shouldThrowOAuthExceptionWhenAuthorizationResponseIsError() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/oauth/authorize")
				.withQueryStringParameters(new Parameter("response_type", "code"),
										   new Parameter("client_id", "client-id"),
										   new Parameter("redirect_uri", "http://my.web.app/oauth/callback"),
										   new Parameter("scope", "read write")))
			.respond(response()
				.withStatusCode(400)
				.withBody(json("{\"error\":\"invalid_client\",\"error_description\":\"client_id unauthorized\"}")));

		provider.provides(request);
	}
}
