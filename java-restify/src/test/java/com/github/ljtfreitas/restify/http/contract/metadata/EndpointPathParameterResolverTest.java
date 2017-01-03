package com.github.ljtfreitas.restify.http.contract.metadata;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointPathParameterResolver;

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

		Object[] args = { "arg" };

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

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenPathArgumentIsNull() {
		parameters.put(new EndpointMethodParameter(0, "first", String.class));

		EndpointPathParameterResolver resolver = new EndpointPathParameterResolver("/method/{first}", parameters);

		Object[] args = { null };

		resolver.resolve(args);
	}

	@Test
	public void shouldResolvePathWithoutDynamicArguments() {
		EndpointPathParameterResolver resolver = new EndpointPathParameterResolver("/method/static/path", parameters);

		Object[] args = new Object[] { "firstArg", "secondArg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/static/path", endpoint);
	}

	@Test
	public void shouldResolveDynamicArgumentWithHyphen() {
		parameters.put(new EndpointMethodParameter(0, "first-argument", String.class));

		EndpointPathParameterResolver resolver = new EndpointPathParameterResolver("/method/{first-argument}", parameters);

		Object[] args = { "arg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/arg", endpoint);
	}

	@Test
	public void shouldResolveDynamicArgumentWithUnderline() {
		parameters.put(new EndpointMethodParameter(0, "first_argument", String.class));

		EndpointPathParameterResolver resolver = new EndpointPathParameterResolver("/method/{first_argument}", parameters);

		Object[] args = { "arg" };

		String endpoint = resolver.resolve(args);

		assertEquals("/method/arg", endpoint);
	}
}
