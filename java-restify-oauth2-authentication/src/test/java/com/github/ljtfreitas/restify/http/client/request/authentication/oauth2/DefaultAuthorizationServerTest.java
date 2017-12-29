package com.github.ljtfreitas.restify.http.client.request.authentication.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.StringBody.exact;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Parameter;

import com.github.ljtfreitas.restify.http.client.message.response.HttpStatusCode;
import com.github.ljtfreitas.restify.http.client.message.response.StatusCode;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessToken;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AccessTokenRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeGrantProperties;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.AuthorizationCodeRequest;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientAuthenticationMethod;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.ClientCredentials;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.DefaultAuthorizationServer;
import com.github.ljtfreitas.restify.http.client.request.authentication.oauth2.GrantProperties;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;

public class DefaultAuthorizationServerTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 8088);

	private MockServerClient mockServerClient;

	private DefaultAuthorizationServer authorizationServer;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 8088);

		authorizationServer = new DefaultAuthorizationServer();
	}

	@Test
	public void shouldAuthorizeOnAuthorizationServer() {
		AuthorizationCodeGrantProperties properties = GrantProperties.Builder.authorizationCode()
				.authorizationUri("http://localhost:8088/oauth/authorize")
				.clientId("client-id")
				.redirectUri("http://my.web.app/oauth/callback")
				.scopes("read", "write")
				.responseType("code")
				.state("current-state")
				.build();

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

		EndpointResponse<String> authorizationResponse = authorizationServer.authorize(new AuthorizationCodeRequest(properties));

		assertNotNull(authorizationResponse);
		assertEquals(StatusCode.of(HttpStatusCode.FOUND), authorizationResponse.status());
		assertEquals("http://my.web.app/oauth/callback?code=abc1234&state=current-state",
				authorizationResponse.headers().get("Location").get().value());
	}

	@Test
	public void shouldGetAccessToken() {
		String authorizationCredentials = "Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ="; //(base64(client_id:client_secret))

		mockServerClient
			.when(request()
					.withMethod("POST")
					.withPath("/oauth/token")
					.withBody(params(new Parameter("grant_type", "client_credentials"),
									 new Parameter("scope", "read write")))
					.withHeader("Authorization", "Basic " + authorizationCredentials))
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"read write\"}")));

		AccessTokenRequest request = AccessTokenRequest
				.clientCredentials(new ClientCredentials("client-id", "client-secret"))
					.accessTokenUri("http://localhost:8088/oauth/token")
					.parameter("scope", "read write")
					.build();

		EndpointResponse<AccessToken> tokenResponse = authorizationServer.requireToken(request);

		assertNotNull(tokenResponse);
		assertEquals(StatusCode.of(HttpStatusCode.OK), tokenResponse.status());
	}

	@Test
	public void shouldGetAccessTokenOnXmlFormat() {
		String authorizationCredentials = "Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ="; //(base64(client_id:client_secret))

		mockServerClient
			.when(request()
					.withMethod("POST")
					.withPath("/oauth/token")
					.withBody(params(new Parameter("grant_type", "client_credentials"),
									 new Parameter("scope", "read write")))
					.withHeader("Authorization", "Basic " + authorizationCredentials))
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/xml")
					.withBody(exact("<oauth>"
									+ "<access_token>aaa111</access_token>"
									+ "<token_type>bearer</token_type>"
									+ "<expires_in>3600</expires_in>"
									+ "<scope>read write</scope>"
								+ "</oauth>")));

		AccessTokenRequest request = AccessTokenRequest
				.clientCredentials(new ClientCredentials("client-id", "client-secret"))
					.accessTokenUri("http://localhost:8088/oauth/token")
					.parameter("scope", "read write")
					.build();

		EndpointResponse<AccessToken> tokenResponse = authorizationServer.requireToken(request);

		assertNotNull(tokenResponse);
		assertEquals(StatusCode.of(HttpStatusCode.OK), tokenResponse.status());
	}

	@Test
	public void shouldGetAccessTokenWithClientCredentialsOnFormParameter() {
		mockServerClient
			.when(request()
					.withMethod("POST")
					.withPath("/oauth/token")
					.withBody(params(new Parameter("grant_type", "client_credentials"),
									 new Parameter("scope", "read write"),
									 new Parameter("client_id", "client-id"),
									 new Parameter("client_secret", "client-secret"))))
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"read write\"}")));

		AccessTokenRequest request = AccessTokenRequest
				.clientCredentials(new ClientCredentials("client-id", "client-secret"))
					.accessTokenUri("http://localhost:8088/oauth/token")
					.parameter("scope", "read write")
					.build();

		authorizationServer = new DefaultAuthorizationServer(ClientAuthenticationMethod.FORM_PARAMETER);

		EndpointResponse<AccessToken> tokenResponse = authorizationServer.requireToken(request);

		assertNotNull(tokenResponse);
		assertEquals(StatusCode.of(HttpStatusCode.OK), tokenResponse.status());
	}

	@Test
	public void shouldGetAccessTokenWithClientCredentialsOnQueryParameter() {
		mockServerClient
			.when(request()
					.withMethod("POST")
					.withPath("/oauth/token")
					.withQueryStringParameters(new Parameter("client_id", "client-id"),
											   new Parameter("client_secret", "client-secret"))
					.withBody(params(new Parameter("grant_type", "client_credentials"),
									 new Parameter("scope", "read write"))))
				.respond(response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"access_token\":\"aaa111\",\"token_type\":\"bearer\",\"expires_in\":3600,\"scope\":\"read write\"}")));

		AccessTokenRequest request = AccessTokenRequest
				.clientCredentials(new ClientCredentials("client-id", "client-secret"))
					.accessTokenUri("http://localhost:8088/oauth/token")
					.parameter("scope", "read write")
					.build();

		authorizationServer = new DefaultAuthorizationServer(ClientAuthenticationMethod.QUERY_PARAMETER);

		EndpointResponse<AccessToken> tokenResponse = authorizationServer.requireToken(request);

		assertNotNull(tokenResponse);
		assertEquals(StatusCode.of(HttpStatusCode.OK), tokenResponse.status());
	}
}
