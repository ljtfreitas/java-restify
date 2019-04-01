package com.github.ljtfreitas.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

public class PathParameterResolverTest {

	private EndpointMethodParameters parameters;

	@Before
	public void setup() {
		parameters = new EndpointMethodParameters();
	}

	@Test
	public void shouldResolveArgumentOnPath() {
		parameters = parameters.put(new EndpointMethodParameter(0, "first", String.class));

		PathParameterResolver resolver = new PathParameterResolver("/method/{first}", parameters);

		Object[] args = { "arg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/arg", endpoint);
	}

	@Test
	public void shouldResolveMultiplesArgumentsOnPath() {
		parameters = parameters.put(new EndpointMethodParameter(0, "first", String.class))
				.put(new EndpointMethodParameter(1, "second", String.class));

		PathParameterResolver resolver = new PathParameterResolver("/method/{first}/{second}", parameters);

		Object[] args = new Object[] { "firstArg", "secondArg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/firstArg/secondArg", endpoint);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenPathArgumentIsNull() {
		parameters = parameters.put(new EndpointMethodParameter(0, "first", String.class));

		PathParameterResolver resolver = new PathParameterResolver("/method/{first}", parameters);

		Object[] args = { null };

		resolver.resolve(args);
	}

	@Test
	public void shouldResolvePathWithoutDynamicArguments() {
		PathParameterResolver resolver = new PathParameterResolver("/method/static/path", parameters);

		Object[] args = new Object[] { "firstArg", "secondArg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/static/path", endpoint);
	}

	@Test
	public void shouldResolveDynamicArgumentWithHyphen() {
		parameters = parameters.put(new EndpointMethodParameter(0, "first-argument", String.class));

		PathParameterResolver resolver = new PathParameterResolver("/method/{first-argument}", parameters);

		Object[] args = { "arg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/arg", endpoint);
	}

	@Test
	public void shouldResolveDynamicArgumentWithUnderline() {
		parameters = parameters.put(new EndpointMethodParameter(0, "first_argument", String.class));

		PathParameterResolver resolver = new PathParameterResolver("/method/{first_argument}", parameters);

		Object[] args = { "arg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/arg", endpoint);
	}

	@Test
	public void shouldResolveSingleDynamicArgument() throws Exception {
		parameters = parameters.put(new EndpointMethodParameter(0, "url", URL.class));

		PathParameterResolver resolver = new PathParameterResolver("{url}", parameters);

		Object[] args = { new URL("http://my.api.com/path") };

		String endpoint = resolver.resolve(args);

		assertEquals("http://my.api.com/path", endpoint);
	}
}
