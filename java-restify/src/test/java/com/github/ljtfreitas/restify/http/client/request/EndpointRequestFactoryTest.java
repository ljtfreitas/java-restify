package com.github.ljtfreitas.restify.http.client.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;
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

	@Mock
	private EndpointRequestInterceptorStack endpointRequestInterceptorStackMock;

	@InjectMocks
	private EndpointRequestFactory endpointRequestFactory;

	@Before
	public void setup() {
		when(endpointRequestInterceptorStackMock.apply(any()))
			.then(returnsFirstArg());
	}

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
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "path", String.class));

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
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "path", String.class));
		endpointMethodParameters.put(new EndpointMethodParameter(1, "secondPath", String.class));
		endpointMethodParameters.put(new EndpointMethodParameter(2, "thirdPath", String.class));

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
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "body", Object.class, EndpointMethodParameterType.BODY));

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
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "header", String.class, EndpointMethodParameterType.HEADER));

		EndpointHeaders endpointHeaders = new EndpointHeaders();
		endpointHeaders.put(new EndpointHeader("X-My-Custom-Header", "{header}"));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("header", new Class[]{String.class}),
				"http://my.api.com/some", "GET", endpointMethodParameters, endpointHeaders);

		Object[] args = new Object[]{"my custom header"};

		EndpointRequest endpointRequest = endpointRequestFactory.createWith(endpointMethod, args);

		assertEquals("http://my.api.com/some", endpointRequest.endpoint().toString());
		assertEquals(endpointMethod.httpMethod(), endpointRequest.method());

		assertFalse(endpointRequest.body().isPresent());

		assertEquals("my custom header", endpointRequest.headers().get("X-My-Custom-Header").get().value());

		assertEquals(JavaType.of(String.class), endpointRequest.responseType());
	}

	@Test
	public void shouldCreateEndpointRequestUsingEndpointMethodWithQueryStringParameter() throws Exception {
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "parameters", Parameters.class, EndpointMethodParameterType.QUERY_STRING,
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
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "parameters", Map.class, EndpointMethodParameterType.QUERY_STRING,
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
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "parameters", Map.class, EndpointMethodParameterType.QUERY_STRING,
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
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "parameter", String.class, EndpointMethodParameterType.QUERY_STRING,
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
		EndpointMethodParameters endpointMethodParameters = new EndpointMethodParameters();
		endpointMethodParameters.put(new EndpointMethodParameter(0, "path", String.class));
		endpointMethodParameters.put(new EndpointMethodParameter(1, "header", String.class, EndpointMethodParameterType.HEADER));
		endpointMethodParameters.put(new EndpointMethodParameter(2, "body", Object.class, EndpointMethodParameterType.BODY));

		EndpointHeaders endpointHeaders = new EndpointHeaders();
		endpointHeaders.put(new EndpointHeader("X-My-Custom-Header", "{header}"));

		EndpointMethod endpointMethod = new EndpointMethod(TargetType.class.getMethod("multiple", new Class[]{String.class, String.class, Object.class}),
				"http://my.api.com/some/{path}", "POST", endpointMethodParameters, endpointHeaders);

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

	interface TargetType {

		public String simple();

		public String path(String path);

		public String path(String path, String secondPath, String thirdPath);

		public String header(String header);

		public String body(Object body);

		public String queryString(Parameters parameters);

		public String queryStringAsMap(Map<String, String> parameters);

		public String queryStringAsMultiValueMap(Map<String, List<String>> parameters);

		public String simpleQueryString(String parameter);

		public String multiple(String path, String header, Object body);

		public EndpointResponse<String> genericType();
	}
}
