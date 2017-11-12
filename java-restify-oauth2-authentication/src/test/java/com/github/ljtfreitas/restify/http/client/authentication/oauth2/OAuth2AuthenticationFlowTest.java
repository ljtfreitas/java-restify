package com.github.ljtfreitas.restify.http.client.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.ParameterBody.params;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.Parameter;

import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.client.authentication.Authentication;
import com.github.ljtfreitas.restify.http.client.message.Cookie;
import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Path;

public class OAuth2AuthenticationFlowTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7085);

	private MockServerClient mockServerClient;

	private String authorizationCredentials;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7085);

		authorizationCredentials = "Y2xpZW50X2lkOmNsaWVudF9zZWNyZXQ="; //(base64(client_id:client_secret))
	}

	@Test
	public void shouldSendRequestToResourceUsingAuthorizationCodeAuthorizationFlow() {
		Authentication authentication = new OAuth2AuthenticationBuilder()
				.grantType()
					.authorizationCode()
						.authorizationUri("http://localhost:7085/oauth/authorize")
						.accessTokenUri("http://localhost:7085/oauth/token")
						.redirectUri("http://my.web.app/oauth/callback")
						.credentials("client_id", "client_secret")
						.scopes("base")
						.state("whatever")
						.cookie(new Cookie("_sid", "xyz"))
						.header(Header.of("X-Auth-Key", "mnb0987"))
					.and()
				.build();

		Api api = new RestifyProxyBuilder()
				.interceptors()
					.authentication(authentication)
				.and()
				.target(Api.class, "http://localhost:7085")
					.build();

		HttpRequest authorizeRequest = request()
				.withMethod("GET")
				.withPath("/oauth/authorize")
				.withQueryStringParameters(new Parameter("response_type", "code"),
						   new Parameter("client_id", "client_id"),
						   new Parameter("redirect_uri", "http://my.web.app/oauth/callback"),
						   new Parameter("scope", "base foo"),
						   new Parameter("state", "whatever"))
				.withCookie("_sid", "xyz")
				.withHeader("X-Auth-Key", "mnb0987");

		mockServerClient
			.when(authorizeRequest)
				.respond(response()
					.withStatusCode(302)
					.withHeader("Content-Type", "application/x-www-form-urlencoded")
					.withHeader("Location", "http://my.web.app/oauth/callback?code=abc123&state=whatever"));

		HttpRequest accessTokenRequest = request()
				.withMethod("POST")
				.withPath("/oauth/token")
				.withBody(params(new Parameter("grant_type", "authorization_code"),
						 new Parameter("code", "abc123"),
						 new Parameter("redirect_uri", "http://my.web.app/oauth/callback")))
				.withHeader("Authorization", "Basic " + authorizationCredentials);

		mockServerClient
			.when(accessTokenRequest)
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"base foo\"}")));

		HttpRequest resourceServerRequest = request()
				.withMethod("GET")
				.withPath("/foo")
				.withHeader("Authorization", "Bearer aaa111");

		mockServerClient
			.when(resourceServerRequest)
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "text/plain")
					.withBody("foo"));

		String output = api.foo();

		assertEquals("foo", output);

		mockServerClient.verify(authorizeRequest, accessTokenRequest, resourceServerRequest);
	}

	@Test
	public void shouldSendRequestToResourceUsingImplicitAuthorizationFlow() {
		Authentication authentication = new OAuth2AuthenticationBuilder()
				.grantType()
					.implicit()
						.authorizationUri("http://localhost:7085/oauth/authorize")
						.accessTokenUri("http://localhost:7085/oauth/token")
						.redirectUri("http://my.web.app/oauth/callback")
						.clientId("client_id")
						.scopes("base")
						.state("whatever")
						.cookie(new Cookie("_sid", "xyz"))
						.header(Header.of("X-Auth-Key", "mnb0987"))
					.and()
				.build();

		Api api = new RestifyProxyBuilder()
				.interceptors()
					.authentication(authentication)
				.and()
				.target(Api.class, "http://localhost:7085")
					.build();

		HttpRequest authorizeRequest = request()
				.withMethod("GET")
				.withPath("/oauth/authorize")
				.withQueryStringParameters(new Parameter("response_type", "token"),
						   new Parameter("client_id", "client_id"),
						   new Parameter("redirect_uri", "http://my.web.app/oauth/callback"),
						   new Parameter("scope", "base foo"),
						   new Parameter("state", "whatever"))
				.withCookie("_sid", "xyz")
				.withHeader("X-Auth-Key", "mnb0987");

		mockServerClient
			.when(authorizeRequest)
				.respond(response()
					.withStatusCode(302)
					.withHeader("Content-Type", "application/x-www-form-urlencoded")
					.withHeader("Location", "http://my.web.app/oauth/callback#access_token=aaa111&token_type=bearer&state=whatever&expires_in=3600&scope=base%20foo"));

		HttpRequest resourceServerRequest = request()
				.withMethod("GET")
				.withPath("/foo")
				.withHeader("Authorization", "Bearer aaa111");

		mockServerClient
			.when(resourceServerRequest)
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "text/plain")
					.withBody("foo"));

		String output = api.foo();

		assertEquals("foo", output);

		mockServerClient.verify(authorizeRequest, resourceServerRequest);
	}

	@Test
	public void shouldSendRequestToResourceUsingResourceOwnerAuthorizationFlow() {
		Authentication authentication = new OAuth2AuthenticationBuilder()
				.grantType()
					.resourceOwner()
						.accessTokenUri("http://localhost:7085/oauth/token")
						.credentials("client_id", "client_secret")
						.resourceOwner("my-username", "my-password")
						.scopes("base")
					.and()
				.build();

		Api api = new RestifyProxyBuilder()
				.interceptors()
					.authentication(authentication)
				.and()
				.target(Api.class, "http://localhost:7085")
					.build();

		HttpRequest accessTokenRequest = request()
				.withMethod("POST")
				.withPath("/oauth/token")
				.withBody(params(new Parameter("grant_type", "password"),
						 new Parameter("scope", "base foo"),
						 new Parameter("username", "my-username"),
						 new Parameter("password", "my-password")))
				.withHeader("Authorization", "Basic " + authorizationCredentials);

		mockServerClient
			.when(accessTokenRequest)
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"base foo\"}")));

		HttpRequest resourceRequest = request()
				.withMethod("GET")
				.withPath("/foo")
				.withHeader("Authorization", "Bearer aaa111");

		mockServerClient
			.when(resourceRequest)
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "text/plain")
					.withBody("foo"));

		String output = api.foo();

		assertEquals("foo", output);

		mockServerClient.verify(accessTokenRequest, resourceRequest);
	}

	@Test
	public void shouldSendRequestToResourceUsingClientCredentialsAuthorizationFlow() {
		Authentication authentication = new OAuth2AuthenticationBuilder()
				.grantType()
					.clientCredentials()
						.accessTokenUri("http://localhost:7085/oauth/token")
						.credentials("client_id", "client_secret")
						.scopes("base")
					.and()
				.build();

		Api api = new RestifyProxyBuilder()
				.interceptors()
					.authentication(authentication)
				.and()
				.target(Api.class, "http://localhost:7085")
					.build();

		authorizationCredentials = "Y2xpZW50X2lkOmNsaWVudF9zZWNyZXQ="; //(base64(client_id:client_secret))

		HttpRequest accessTokenRequest = request()
				.withMethod("POST")
				.withPath("/oauth/token")
				.withBody(params(new Parameter("grant_type", "client_credentials"),
								 new Parameter("scope", "base foo")))
				.withHeader("Authorization", "Basic " + authorizationCredentials);

		mockServerClient
			.when(accessTokenRequest)
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"base foo\"}")));

		HttpRequest resourceRequest = request()
				.withMethod("GET")
				.withPath("/foo")
				.withHeader("Authorization", "Bearer aaa111");

		mockServerClient
			.when(resourceRequest)
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "text/plain")
					.withBody("foo"));

		String output = api.foo();

		assertEquals("foo", output);

		mockServerClient.verify(accessTokenRequest, resourceRequest);
	}

	interface Api {

		@Get
		@Path("/foo")
		@Scope("foo")
		String foo();
	}
}
