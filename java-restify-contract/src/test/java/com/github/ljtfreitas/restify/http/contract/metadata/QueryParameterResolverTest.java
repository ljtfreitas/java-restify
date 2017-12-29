package com.github.ljtfreitas.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.QueryParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;

public class QueryParameterResolverTest {

	private EndpointMethodParameters parameters;

	private QueryParameterResolver resolver;

	@Before
	public void setup() {
		parameters = new EndpointMethodParameters();

		parameters.put(new EndpointMethodParameter(0, "param1", String.class,
				EndpointMethodParameterType.QUERY_STRING, new QueryParameterSerializer()));

		parameters.put(new EndpointMethodParameter(1, "param2", String.class,
				EndpointMethodParameterType.QUERY_STRING, new QueryParameterSerializer()));

		resolver = new QueryParameterResolver(parameters.ofQuery());
	}

	@Test
	public void shouldGenerateQueryStringUsingMethodQueryParameters() {
		String[] args = {"value1", "value2"};

		String query = resolver.resolve(args);

		assertEquals("?param1=value1&param2=value2", query);
	}

	@Test
	public void shouldGenerateQueryStringUsingMethodQueryParametersIgnoringNullArgument() {
		String[] args = {"value1", null};

		QueryParameterResolver resolver = new QueryParameterResolver(parameters.ofQuery());
		String query = resolver.resolve(args);

		assertEquals("?param1=value1", query);
	}
}
