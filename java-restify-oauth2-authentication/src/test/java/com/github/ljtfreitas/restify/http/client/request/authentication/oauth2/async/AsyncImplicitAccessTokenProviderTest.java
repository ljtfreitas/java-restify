package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.async;

import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenType;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ImplicitGrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2AuthenticatedEndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2Exception;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.OAuth2UserApprovalRequiredException;

public class AsyncImplicitAccessTokenProviderTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8088);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MockServerClient mockServerClient;

	private AsyncImplicitAccessTokenProvider provider;

	private OAuth2AuthenticatedEndpointRequest request;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8088);

		ImplicitGrantProperties properties = GrantProperties.Builder.implicit()
				.authorizationUri("http://localhost:8088/oauth/authorize")
				.clientId("client-id")
				.redirectUri("http://my.web.app/oauth/callback")
				.scopes("read", "write")
				.responseType("token")
				.state("current-state")
				.build();

		EndpointRequest source = new EndpointRequest(URI.create("http://my.resource.server/path"), "GET");

		request = new OAuth2AuthenticatedEndpointRequest(source, properties);

		provider = new AsyncImplicitAccessTokenProvider();
	}

	@Test
	public void shouldGetAuthorizationCodeFromRedirectLocationUri() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/oauth/authorize")
				.withQueryStringParameters(new Parameter("response_type", "token"),
										   new Parameter("client_id", "client-id"),
										   new Parameter("redirect_uri", "http://my.web.app/oauth/callback"),
										   new Parameter("scope", "read write")))
			.respond(response()
				.withStatusCode(302)
				.withHeader("Content-Type", "application/x-www-form-urlencoded")
				.withHeader("Location", "http://my.web.app/oauth/callback#access_token=abc1234&token_type=bearer&state=current-state&expires_in=3600&scope=read%20write"));

		CompletableFuture<AccessToken> accessTokenAsFuture = provider.provides(request).toCompletableFuture();

		AccessToken accessToken = accessTokenAsFuture.join();

		assertEquals(AccessTokenType.BEARER, accessToken.type());
		assertEquals("abc1234", accessToken.token());

		assertEquals("read write", accessToken.scope());

		Instant expectedExpiration = Instant.now().plus(Duration.ofSeconds(3600));
		assertEquals(expectedExpiration.truncatedTo(ChronoUnit.SECONDS), accessToken.expiration().get().truncatedTo(ChronoUnit.SECONDS));
	}

	@Test
	public void shouldThrowUserApprovalRequiredExceptionWhenAuthorizationResponseIsOk() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/oauth/authorize")
				.withQueryStringParameters(new Parameter("response_type", "token"),
										   new Parameter("client_id", "client-id"),
										   new Parameter("redirect_uri", "http://my.web.app/oauth/callback"),
										   new Parameter("scope", "read write")))
			.respond(response()
				.withStatusCode(200));

		CompletableFuture<AccessToken> accessTokenAsFuture = provider.provides(request).toCompletableFuture();

		expectedException.expectCause(isA(OAuth2UserApprovalRequiredException.class));

		accessTokenAsFuture.join();
	}

	@Test
	public void shouldThrowOAuthExceptionWhenAuthorizationResponseIsError() {
		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/oauth/authorize")
				.withQueryStringParameters(new Parameter("response_type", "token"),
										   new Parameter("client_id", "client-id"),
										   new Parameter("redirect_uri", "http://my.web.app/oauth/callback"),
										   new Parameter("scope", "read write")))
			.respond(response()
				.withStatusCode(400)
				.withBody(json("{\"error\":\"invalid_client\",\"error_description\":\"client_id unauthorized\"}")));

		CompletableFuture<AccessToken> accessTokenAsFuture = provider.provides(request).toCompletableFuture();

		expectedException.expectCause(isA(OAuth2Exception.class));

		accessTokenAsFuture.join();
	}
}
