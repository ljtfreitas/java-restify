package com.github.ljtfreitas.restify.http.contract;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.Parameters.Parameter;

public class ParametersTest {

	private Parameters parameters;

	@Before
	public void setup() {
		parameters = new Parameters().put("param", "value");
	}

	@Test
	public void mustCreateNewParametersInstanceWhenParameterIsAdded() {
		Parameter newParameter = Parameter.of("new-param", "new-value");

		Parameters output = parameters.put(newParameter);

		assertThat(parameters, not(hasItem(newParameter)));
		assertThat(output, hasItem(newParameter));
	}

	@Test
	public void mustCreateNewParametersInstanceWhenParameterIsAddedUsingNameAndValue() {
		Parameter newParameter = Parameter.of("new-param", "new-value");

		Parameters output = parameters.put(newParameter.name(), newParameter.value());

		assertThat(parameters, not(hasItem(newParameter)));
		assertThat(output, hasItem(newParameter));
	}

	@Test
	public void mustCreateNewParametersInstanceWhenParameterIsAddedUsingNameAndCollectionOfValues() {
		Parameter newParameter = Parameter.of("new-param", Arrays.asList("value1", "value2"));

		Parameters output = parameters.put(newParameter);

		assertThat(parameters, not(hasItem(newParameter)));

		assertThat(output, hasItem(newParameter));
	}

	@Test
	public void mustCopyParametersWhenNewParameterIsAdded() {
		Parameter newParameter = Parameter.of("new-parameter", "new-value");

		Parameters output = parameters.put(newParameter);

		assertThat(parameters, not(hasItem(newParameter)));

		assertThat(output, hasItem(Parameter.of("param", "value")));
		assertThat(output, hasItem(newParameter));
	}

	@Test
	public void mustCreateNewParametersInstanceWhenParameterAlreadyPresentIsAdded() {
		Parameters output = parameters.put("param", "value2");

		Parameter newParameterAsCollection = Parameter.of("param", Arrays.asList("value", "value2"));

		assertThat(parameters, hasItem(Parameter.of("param", "value")));
		assertThat(parameters, not(hasItem(newParameterAsCollection)));

		assertThat(output, hasItem(newParameterAsCollection));
	}

	@Test
	public void mustCreateNewParametersInstanceFromFactoryMethodUsingCollection() {
		Parameters output = Parameters.of(Arrays.asList(Parameter.of("param1", "value1"),
				Parameter.of("param2", Arrays.asList("value2", "value3"))));

		Optional<Parameter> parameter = output.get("param1");
		assertTrue(parameter.isPresent());
		assertEquals("value1", parameter.get().value());

		parameter = output.get("param2");
		assertTrue(parameter.isPresent());
		assertThat(parameter.get().values(), hasItems("value2", "value3"));
	}

	@Test
	public void mustCreateNewParametersInstanceFromFactoryMethodUsingMap() {
		Map<String, Object> parameters = new LinkedHashMap<>();
		parameters.put("param1", "value1");
		parameters.put("param2", Arrays.asList("value2", "value3"));

		Parameters output = Parameters.of(parameters);

		Optional<Parameter> parameter = output.get("param1");
		assertTrue(parameter.isPresent());
		assertEquals("value1", parameter.get().value());

		parameter = output.get("param2");
		assertTrue(parameter.isPresent());
		assertThat(parameter.get().values(), hasItems("value2", "value3"));
	}

	@Test
	public void mustCreateNewParametersInstanceFromFactoryMethodUsingArray() {
		Parameters output = Parameters.of(Parameter.of("param1", "value1"), Parameter.of("param2", Arrays.asList("value2", "value3")));

		Optional<Parameter> parameter = output.get("param1");
		assertTrue(parameter.isPresent());
		assertEquals("value1", parameter.get().value());

		parameter = output.get("param2");
		assertTrue(parameter.isPresent());
		assertThat(parameter.get().values(), hasItems("value2", "value3"));
	}

	@Test
	public void mustCreateParameterUsingPrefix() {
		parameters = new Parameters("bla.");

		Parameters output = parameters.put("param", "value");

		assertThat(parameters, emptyIterable());
		assertThat(output, hasItem(Parameter.of("bla.param", "value")));
	}
}
