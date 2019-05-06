package com.github.ljtfreitas.restify.http;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.xerces.util.URI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Header;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointVersion;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.contract.QueryParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.QueryParametersSerializer;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointHeader;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointHeaders;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class EndpointRequestFactoryTest {

	@InjectMocks
	private EndpointRequestFactory endpointRequestFactory;

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithoutParameters() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("simple"), "http://my.api.com/some",
				"GET");

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, new Object[0]);

		assertEquals(endpointMethod.path(), endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithDynamicPathParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "path", String.class));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("path", new Class[]{String.class}),
				"http://my.api.com/some/{path}", "GET", endpointMethodParameters);

		Object[] args = new Object[]{"argument"};

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/some/argument", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithMultiplesDynamicPathParameters() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "path", String.class))
				.put(new EndpointMethodParameter(1, "secondPath", String.class))
				.put(new EndpointMethodParameter(2, "thirdPath", String.class));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("path", new Class[]{String.class, String.class, String.class}),
				"http://my.api.com/some/{path}/{secondPath}/{thirdPath}", "GET", endpointMethodParameters);

		Object[] args = new Object[]{"my-first-path", "my-second-path", "last-but-not-least-third-path"};

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/some/my-first-path/my-second-path/last-but-not-least-third-path",
				endpointRequest.endpoint().toString());

		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithBodyParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "body", Object.class, EndpointMethodParameterType.BODY));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("body", new Class[]{Object.class}),
				"http://my.api.com/some", "POST", endpointMethodParameters);

		Object[] args = new Object[]{"my body"};

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/some", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertTrue(endpointRequest.body().isPresent());
		assertEquals("my body", endpointRequest.body().get());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithDynamicHeaderParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "X-My-Custom-Header", String.class, EndpointMethodParameterType.HEADER));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("header", new Class[]{String.class}),
				"http://my.api.com/some", "GET", endpointMethodParameters);

		Object[] args = new Object[]{"my custom header"};

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/some", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals("my custom header", endpointRequest.headers().get("X-My-Custom-Header").get().value());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithDynamicCookieParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "JSESSIONID", String.class, EndpointMethodParameterType.COOKIE));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("cookie", new Class[]{String.class}),
				"http://my.api.com/some", "GET", endpointMethodParameters);

		Object[] args = new Object[]{"abc1234"};

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/some", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals("JSESSIONID=abc1234", endpointRequest.headers().get("Cookie").get().value());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithQueryStringParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "parameters", Parameters.class, EndpointMethodParameterType.QUERY_STRING,
						new QueryParametersSerializer()));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("queryString", new Class[]{Parameters.class}),
				"http://my.api.com/query", "GET", endpointMethodParameters);

		Parameters parameters = new Parameters()
				.put("param1", "value1")
				.put("param2", "value2");

		Object[] args = new Object[]{parameters};

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/query?param1=value1&param2=value2", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithQueryStringParameterAsMap() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "parameters", Map.class, EndpointMethodParameterType.QUERY_STRING,
						new QueryParametersSerializer()));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("queryStringAsMap", new Class[]{Map.class}),
				"http://my.api.com/query", "GET", endpointMethodParameters);

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("param1", "value1");
		parameters.put("param2", "value2");

		Object[] args = new Object[]{parameters};

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/query?param1=value1&param2=value2", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithQueryStringParameterAsMapWithMultiplesValues() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "parameters", Map.class, EndpointMethodParameterType.QUERY_STRING,
						new QueryParametersSerializer()));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("queryStringAsMultiValueMap", new Class[]{Map.class}),
				"http://my.api.com/query", "GET", endpointMethodParameters);

		Map<String, List<String>> parameters = new LinkedHashMap<>();
		parameters.put("param1", Arrays.asList("value1", "value2"));
		parameters.put("param2", Arrays.asList("value3", "value4"));

		Object[] args = new Object[]{parameters};

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/query?param1=value1&param1=value2&param2=value3&param2=value4",
				endpointRequest.endpoint().toString());

		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithSimpleQueryStringParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "parameter", String.class, EndpointMethodParameterType.QUERY_STRING,
						new QueryParameterSerializer()));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("simpleQueryString", new Class[]{String.class}),
				"http://my.api.com/query", "GET", endpointMethodParameters);

		String parameter = "value";

		Object[] args = new Object[] { parameter };

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/query?parameter=value", endpointRequest.endpoint().toString());

		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestFactoryUsingEndpointMethodWithMultiplesTypesOfDynamicParameters() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "path", String.class))
				.put(new EndpointMethodParameter(1, "X-My-Custom-Header", String.class, EndpointMethodParameterType.HEADER))
				.put(new EndpointMethodParameter(2, "body", Object.class, EndpointMethodParameterType.BODY));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("multiple", new Class[]{String.class, String.class, Object.class}),
				"http://my.api.com/some/{path}", "POST", endpointMethodParameters);

		Object[] args = new Object[]{"argument", "my custom header", "my body"};

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/some/argument", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertTrue(endpointRequest.body().isPresent());
		assertEquals("my body", endpointRequest.body().get());

		assertEquals("my custom header", endpointRequest.headers().get("X-My-Custom-Header").get().value());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestWithResponseTypeDifferentOfTheMethodReturnType() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("genericType"), "http://my.api.com/some",
				"GET");

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, new Object[0], JavaType.of(String.class));

		assertEquals(endpointMethod.path(), endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertTrue(endpointRequest.headers().all().isEmpty());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestWithVersion() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("simple"), "http://my.api.com/some",
				"GET", "v1");

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, new Object[0], JavaType.of(String.class));

		assertTrue(endpointRequest.version().isPresent());

		EndpointVersion version = endpointRequest.version().get();
		assertEquals("v1", version.get());
	}

	@Test
	public void shouldMergeQueryParametersPresenOnEnpointPathWithMethodParameters() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters()
				.put(new EndpointMethodParameter(0, "parameter", String.class, EndpointMethodParameterType.QUERY_STRING,
						new QueryParameterSerializer()));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("simpleQueryString", String.class),
				"http://my.api.com/some?a=value&b=other-value", "GET", endpointMethodParameters);

		Object[] args = new Object[] { "my-query-parameter" };

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args, JavaType.of(String.class));

		assertEquals(new URI("http://my.api.com/some?a=value&b=other-value&parameter=my-query-parameter").toString(),
				endpointRequest.endpoint().toString());
	}

	@Test
	public void shouldApplyEndpointMethodHeaders() throws Exception {
		EndpointHeaders headers = new EndpointHeaders()
				.put(new EndpointHeader("X-My-Header", "my-header"))
				.put(new EndpointHeader("X-Custom-Header", "custom-header"));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("simple"), "http://my.api.com/some",
				"GET", new EndpointMethodParameters(), headers);

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, new Object[0]);

		assertEquals(endpointMethod.path(), endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());
		assertEquals(JavaType.of(String.class), endpointRequest.responseType());

		assertThat(endpointRequest.headers().all(), hasSize(2));

		Optional<Header> myHeader = endpointRequest.headers().get("X-My-Header");
		assertTrue(myHeader.isPresent());
		assertEquals("my-header", myHeader.get().value());

		Optional<Header> customHeader = endpointRequest.headers().get("X-Custom-Header");
		assertTrue(customHeader.isPresent());
		assertEquals("custom-header", customHeader.get().value());
	}

	interface TargetType {

		public String simple();

		public String path(String path);

		public String path(String path, String secondPath, String thirdPath);

		public String header(String header);

		public String cookie(String cookie);

		public String body(Object body);

		public String queryString(Parameters parameters);

		public String queryStringAsMap(Map<String, String> parameters);

		public String queryStringAsMultiValueMap(Map<String, List<String>> parameters);

		public String simpleQueryString(String parameter);

		public String multiple(String path, String header, Object body);

		public EndpointResponse<String> genericType();
	}
}
