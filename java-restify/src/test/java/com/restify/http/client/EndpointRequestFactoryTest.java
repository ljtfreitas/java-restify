package com.restify.http.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.restify.http.metadata.EndpointHeader;
import com.restify.http.metadata.EndpointHeaders;
import com.restify.http.metadata.EndpointMethod;
import com.restify.http.metadata.EndpointMethodParameter;
import com.restify.http.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.restify.http.metadata.EndpointMethodParameters;

public class EndpointRequestFactoryTest {

	@Test
	public void shouldCreateEndpointRequestFactoryUsingEndpointMethodWithoutParameters() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("simple"), "http://my.api.com/some",
				"GET");

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).buildWith(new Object[0]);

		assertEquals(endpointMethod.path(), endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestFactoryUsingEndpointMethodWithDynamicPathParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "path"));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("path", new Class[]{String.class}),
				"http://my.api.com/some/{path}", "GET", endpointMethodParameters);

		Object[] args = new Object[]{"argument"};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).buildWith(args);

		assertEquals("http://my.api.com/some/argument", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestFactoryUsingEndpointMethodWithMultiplesDynamicPathParameters() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "path"));
		endpointMethodParameters.put(new EndpointMethodParameter(1, "secondPath"));
		endpointMethodParameters.put(new EndpointMethodParameter(2, "thirdPath"));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("path", new Class[]{String.class, String.class, String.class}),
				"http://my.api.com/some/{path}/{secondPath}/{thirdPath}", "GET", endpointMethodParameters);

		Object[] args = new Object[]{"my-first-path", "my-second-path", "last-but-not-least-third-path"};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).buildWith(args);

		assertEquals("http://my.api.com/some/my-first-path/my-second-path/last-but-not-least-third-path",
				endpointRequest.endpoint().toString());

		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestFactoryUsingEndpointMethodWithBodyParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "body", EndpointMethodParameterType.BODY));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("body", new Class[]{Object.class}),
				"http://my.api.com/some", "POST", endpointMethodParameters);

		Object[] args = new Object[]{"my body"};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).buildWith(args);

		assertEquals("http://my.api.com/some", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertTrue(endpointRequest.body().isPresent());
		assertEquals("my body", endpointRequest.body().get());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestFactoryUsingEndpointMethodWithDynamicHeaderParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "header", EndpointMethodParameterType.HEADER));

		EndpointHeaders endpointHeaders = new EndpointHeaders();
		endpointHeaders.put(new EndpointHeader("X-My-Custom-Header", "{header}"));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("header", new Class[]{String.class}),
				"http://my.api.com/some", "GET", endpointMethodParameters, endpointHeaders);

		Object[] args = new Object[]{"my custom header"};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).buildWith(args);

		assertEquals("http://my.api.com/some", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals("my custom header", endpointRequest.headers().get("X-My-Custom-Header").get());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestFactoryUsingEndpointMethodWithMultiplesTypesOfDynamicParameters() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "path"));
		endpointMethodParameters.put(new EndpointMethodParameter(1, "header", EndpointMethodParameterType.HEADER));
		endpointMethodParameters.put(new EndpointMethodParameter(2, "body", EndpointMethodParameterType.BODY));

		EndpointHeaders endpointHeaders = new EndpointHeaders();
		endpointHeaders.put(new EndpointHeader("X-My-Custom-Header", "{header}"));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("multiple", new Class[]{String.class, String.class, Object.class}),
				"http://my.api.com/some/{path}", "POST", endpointMethodParameters, endpointHeaders);

		Object[] args = new Object[]{"argument", "my custom header", "my body"};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).buildWith(args);

		assertEquals("http://my.api.com/some/argument", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertTrue(endpointRequest.body().isPresent());
		assertEquals("my body", endpointRequest.body().get());

		assertEquals("my custom header", endpointRequest.headers().get("X-My-Custom-Header").get());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	interface TargetType {

		public String simple();

		public String path(String path);

		public String path(String path, String secondPath, String thirdPath);

		public String header(String header);

		public String body(Object body);

		public String multiple(String path, String header, Object body);
	}
}
