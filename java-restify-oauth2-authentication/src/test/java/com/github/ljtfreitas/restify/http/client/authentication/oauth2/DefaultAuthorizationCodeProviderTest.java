package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

public class DefaultAuthorizationCodeProviderTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8088);

	private MockServerClient mockServerClient;

	private DefaultAuthorizationCodeProvider provider;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8088);

		OAuth2AuthorizationConfiguration configuration = new OAuth2AuthorizationConfiguration.Builder()
				.authorizationUri("http://localhost:8088/oauth/authorize")
				.clientId("client-id")
				.redirectUri("http://my.web.app/oauth/callback")
				.scopes("read", "write")
				.build();

		provider = new DefaultAuthorizationCodeProvider(configuration);
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
										   new Parameter("scope", "read write")))
			.respond(response()
				.withStatusCode(302)
				.withHeader("Content-Type", "application/x-www-form-urlencoded")
				.withHeader("Location", "http://my.web.app/oauth/callback?code=abc1234"));

		assertEquals("abc1234", provider.get());
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

		provider.get();
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

		provider.get();
	}
}
