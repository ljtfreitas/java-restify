package com.restify.http.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.restify.http.metadata.EndpointHeader;
import com.restify.http.metadata.EndpointHeaders;
import com.restify.http.metadata.EndpointMethod;
import com.restify.http.metadata.EndpointMethodParameter;
import com.restify.http.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.restify.http.metadata.EndpointMethodParameters;
import com.restify.http.metadata.EndpointMethodQueryParametersSerializer;
import com.restify.http.metadata.Parameters;

public class EndpointRequestFactoryTest {

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithoutParameters() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("simple"), "http://my.api.com/some",
				"GET");

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).createWith(new Object[0]);

		assertEquals(endpointMethod.path(), endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithDynamicPathParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "path"));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("path", new Class[]{String.class}),
				"http://my.api.com/some/{path}", "GET", endpointMethodParameters);

		Object[] args = new Object[]{"argument"};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).createWith(args);

		assertEquals("http://my.api.com/some/argument", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithMultiplesDynamicPathParameters() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "path"));
		endpointMethodParameters.put(new EndpointMethodParameter(1, "secondPath"));
		endpointMethodParameters.put(new EndpointMethodParameter(2, "thirdPath"));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("path", new Class[]{String.class, String.class, String.class}),
				"http://my.api.com/some/{path}/{secondPath}/{thirdPath}", "GET", endpointMethodParameters);

		Object[] args = new Object[]{"my-first-path", "my-second-path", "last-but-not-least-third-path"};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).createWith(args);

		assertEquals("http://my.api.com/some/my-first-path/my-second-path/last-but-not-least-third-path",
				endpointRequest.endpoint().toString());

		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithBodyParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "body", EndpointMethodParameterType.BODY));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("body", new Class[]{Object.class}),
				"http://my.api.com/some", "POST", endpointMethodParameters);

		Object[] args = new Object[]{"my body"};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).createWith(args);

		assertEquals("http://my.api.com/some", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertTrue(endpointRequest.body().isPresent());
		assertEquals("my body", endpointRequest.body().get());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithDynamicHeaderParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "header", EndpointMethodParameterType.HEADER));

		EndpointHeaders endpointHeaders = new EndpointHeaders();
		endpointHeaders.put(new EndpointHeader("X-My-Custom-Header", "{header}"));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("header", new Class[]{String.class}),
				"http://my.api.com/some", "GET", endpointMethodParameters, endpointHeaders);

		Object[] args = new Object[]{"my custom header"};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).createWith(args);

		assertEquals("http://my.api.com/some", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals("my custom header", endpointRequest.headers().get("X-My-Custom-Header").get());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithQueryStringParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "parameters", EndpointMethodParameterType.QUERY_STRING,
				new EndpointMethodQueryParametersSerializer()));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("queryString", new Class[]{Parameters.class}),
				"http://my.api.com/query", "GET", endpointMethodParameters);

		Parameters parameters = new Parameters();
		parameters.put("param1", "value1");
		parameters.put("param2", "value2");

		Object[] args = new Object[]{parameters};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).createWith(args);

		assertEquals("http://my.api.com/query?param1=value1&param2=value2", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithQueryStringParameterAsMap() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "parameters", EndpointMethodParameterType.QUERY_STRING,
				new EndpointMethodQueryParametersSerializer()));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("queryStringAsMap", new Class[]{Map.class}),
				"http://my.api.com/query", "GET", endpointMethodParameters);

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("param1", "value1");
		parameters.put("param2", "value2");

		Object[] args = new Object[]{parameters};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).createWith(args);

		assertEquals("http://my.api.com/query?param1=value1&param2=value2", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals(String.class, endpointRequest.expectedType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithQueryStringParameterAsMapWithMultiplesValues() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "parameters", EndpointMethodParameterType.QUERY_STRING,
				new EndpointMethodQueryParametersSerializer()));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("queryStringAsMultiValueMap", new Class[]{Map.class}),
				"http://my.api.com/query", "GET", endpointMethodParameters);

		Map<String, List<String>> parameters = new LinkedHashMap<>();
		parameters.put("param1", Arrays.asList("value1", "value2"));
		parameters.put("param2", Arrays.asList("value3", "value4"));

		Object[] args = new Object[]{parameters};

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).createWith(args);

		assertEquals("http://my.api.com/query?param1=value1&param1=value2&param2=value3&param2=value4",
				endpointRequest.endpoint().toString());

		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

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

		EndpointRequest endpointRequest = new EndpointRequestFactory(endpointMethod).createWith(args);

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

		public String queryString(Parameters parameters);

		public String queryStringAsMap(Map<String, String> parameters);

		public String queryStringAsMultiValueMap(Map<String, List<String>> parameters);

		public String multiple(String path, String header, Object body);
	}
}
