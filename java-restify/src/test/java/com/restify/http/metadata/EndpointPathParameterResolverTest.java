package com.restify.http.metadata;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.metadata.EndpointMethodParameter;
import com.restify.http.metadata.EndpointMethodParameters;
import com.restify.http.metadata.EndpointPathParameterResolver;

public class EndpointPathParameterResolverTest {

	private EndpointMethodParameters parameters;

	@Before
	public void setup() {
		parameters = new EndpointMethodParameters();
	}

	@Test
	public void shouldResolveArgumentOnPath() {
		parameters.put(new EndpointMethodParameter(0, "first", String.class));

		EndpointPathParameterResolver resolver = new EndpointPathParameterResolver("/method/{first}", parameters);

		Object[] args = new Object[] { "arg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/arg", endpoint);
	}

	@Test
	public void shouldResolveMultiplesArgumentsOnPath() {
		parameters.put(new EndpointMethodParameter(0, "first", String.class));
		parameters.put(new EndpointMethodParameter(1, "second", String.class));

		EndpointPathParameterResolver resolver = new EndpointPathParameterResolver("/method/{first}/{second}", parameters);

		Object[] args = new Object[] { "firstArg", "secondArg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/firstArg/secondArg", endpoint);
	}

	@Test
	public void shouldResolvePathWithoutDynamicArguments() {
		EndpointPathParameterResolver resolver = new EndpointPathParameterResolver("/method/static/path", parameters);

		Object[] args = new Object[] { "firstArg", "secondArg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/static/path", endpoint);
	}
}
