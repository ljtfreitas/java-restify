package com.github.ljtfreitas.restify.cdi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Path;

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {Extension.class, RestifyCdiExtension.class, SimpleEndpointRequestInterceptor.class})
public class RestifyCdiExtensionExtendedTest {

	@Restifyable(endpoint = "http://localhost:8090/api")
	public interface MyApiType {

		@Path("/test")
		@Get
		String test();
	}

	@Inject
	private MyApiType myApiType;

	private ClientAndServer mockServer;

	@Before
	public void setup() {
		mockServer = startClientAndServer(8090);

		MockServerClient mockServerClient = new MockServerClient("localhost", 8090);

		mockServerClient
			.when(request()
				.withMethod("GET")
				.withPath("/api/test"))
			.respond(response()
				.withStatusCode(200)
				.withHeader("Content-Type", "text/plain; charset=UTF-8")
				.withBody("Hello, Restify CDI extension it's works!"));
	}

	@After
	public void after() {
		mockServer.stop();
	}

	@Test
	public void shouldInjectRestifyableType() {
		assertNotNull(myApiType);

		assertEquals("Hello, Restify CDI extension it's works!", myApiType.test());
	}
}
