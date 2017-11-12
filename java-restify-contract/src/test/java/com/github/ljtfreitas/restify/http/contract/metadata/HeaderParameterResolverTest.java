package com.github.ljtfreitas.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;

public class HeaderParameterResolverTest {

	private EndpointMethodParameters parameters;

	@Before
	public void setup() {
		parameters = new EndpointMethodParameters();
	}

	@Test
	public void shouldResolveDynamicHeaderArgument() {
		parameters.put(new EndpointMethodParameter(0, "contentType", String.class, EndpointMethodParameterType.HEADER));

		HeaderParameterResolver resolver = new HeaderParameterResolver("{contentType}", parameters);

		Object[] args = new Object[] { "application/json" };

		String value = resolver.resolve(args);

		assertEquals(args[0], value);
	}

	@Test
	public void shouldResolveDynamicHeaderArgumentWithHyphen() {
		parameters.put(new EndpointMethodParameter(0, "content-type", String.class, EndpointMethodParameterType.HEADER));

		HeaderParameterResolver resolver = new HeaderParameterResolver("{content-type}", parameters);

		Object[] args = new Object[] { "application/json" };

		String value = resolver.resolve(args);

		assertEquals(args[0], value);
	}

	@Test
	public void shouldResolveDynamicHeaderArgumentWithUnderline() {
		parameters.put(new EndpointMethodParameter(0, "content_type", String.class, EndpointMethodParameterType.HEADER));

		HeaderParameterResolver resolver = new HeaderParameterResolver("{content_type}", parameters);

		Object[] args = new Object[] { "application/json" };

		String value = resolver.resolve(args);

		assertEquals(args[0], value);
	}

	@Test
	public void shouldResolveToEmptyWhenDynamicHeaderArgumentValueIsNull() {
		parameters.put(new EndpointMethodParameter(0, "contentType", String.class, EndpointMethodParameterType.HEADER));

		HeaderParameterResolver resolver = new HeaderParameterResolver("{contentType}", parameters);

		Object[] args = new Object[] { null };

		String value = resolver.resolve(args);

		assertEquals("", value);
	}

	@Test
	public void shouldResolveStaticHeaderArgument() {
		parameters.put(new EndpointMethodParameter(0, "any", String.class, EndpointMethodParameterType.PATH));

		HeaderParameterResolver resolver = new HeaderParameterResolver("application/json", parameters);

		Object[] args = new Object[] { "no header argument" };

		String value = resolver.resolve(args);

		assertEquals("application/json", value);
	}
}
