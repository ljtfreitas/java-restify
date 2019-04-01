package com.github.ljtfreitas.restify.http.client.hateoas.browser;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class LinkURITemplateParametersTest {

	private LinkURITemplateParameters parameters;

	@Before
	public void setup() {
		parameters = new LinkURITemplateParameters().put("param", "value");
	}

	@Test
	public void mustCreateNewParametersInstanceWhenParameterIsAdded() {
		LinkURITemplateParameter newParameter = LinkURITemplateParameter.using("new-param", "new-value");

		LinkURITemplateParameters output = parameters.put(newParameter);

		assertThat(parameters, not(hasItem(newParameter)));
		assertThat(output, hasItem(newParameter));
	}

	@Test
	public void mustCreateNewParametersInstanceWhenParameterIsAddedUsingNameAndValue() {
		LinkURITemplateParameter newParameter = LinkURITemplateParameter.using("new-param", "new-value");

		LinkURITemplateParameters output = parameters.put(newParameter.name(), newParameter.value());

		assertThat(parameters, not(hasItem(newParameter)));
		assertThat(output, hasItem(newParameter));
	}

	@Test
	public void mustCopyParametersWhenNewParameterIsAdded() {
		LinkURITemplateParameter newParameter = LinkURITemplateParameter.using("new-parameter", "new-value");

		LinkURITemplateParameters output = parameters.put(newParameter);

		assertThat(parameters, not(hasItem(newParameter)));

		assertThat(output, hasItem(LinkURITemplateParameter.using("param", "value")));
		assertThat(output, hasItem(newParameter));
	}

	@Test
	public void mustCreateNewParametersInstanceUsingMap() {
		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("param1", "value1");
		parameters.put("param2", "value2");

		LinkURITemplateParameters output = new LinkURITemplateParameters(parameters);

		Optional<String> parameter = output.get("param1");
		assertTrue(parameter.isPresent());
		assertEquals("value1", parameter.get());

		parameter = output.get("param2");
		assertTrue(parameter.isPresent());
		assertEquals(parameter.get(), "value2");
	}

	@Test
	public void mustCreateNewParametersInstanceFromFactoryMethodUsingArray() {
		LinkURITemplateParameters output = new LinkURITemplateParameters(LinkURITemplateParameter.using("param1", "value1"),
				LinkURITemplateParameter.using("param2", "value2"));

		Optional<String> parameter = output.get("param1");
		assertTrue(parameter.isPresent());
		assertEquals("value1", parameter.get());

		parameter = output.get("param2");
		assertTrue(parameter.isPresent());
		assertEquals(parameter.get(), "value2");
	}
}
