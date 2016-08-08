package com.restify.http.metadata;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class EndpointMethodQueryParametersSerializerTest {

	private EndpointMethodQueryParametersSerializer serializer;

	@Before
	public void setup() {
		serializer = new EndpointMethodQueryParametersSerializer();
	}

	@Test
	public void shouldSerializeParametersObjectToQueryParametersFormat() {
		Parameters parameters = new Parameters();
		parameters.put("param1", "value1");
		parameters.put("param1", "value2");
		parameters.put("param2", "value3");

		String result = serializer.serialize(parameters);

		assertEquals("param1=value1&param1=value2&param2=value3", result);
	}

	@Test
	public void shouldSerializeMapToQueryParametersFormat() {
		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("param1", "value1");
		parameters.put("param2", "value2");
		parameters.put("param3", "value3");

		String result = serializer.serialize(parameters);

		assertEquals("param1=value1&param2=value2&param3=value3", result);
	}

	@Test
	public void shouldSerializeMapOfIterableToQueryParametersFormat() {
		Map<String, Collection<String>> parameters = new LinkedHashMap<>();
		parameters.put("param1", Arrays.asList("value1", "value2", "value3"));
		parameters.put("param2", Arrays.asList("value4", "value5"));
		parameters.put("param3", Arrays.asList("value6"));

		String result = serializer.serialize(parameters);

		assertEquals("param1=value1&param1=value2&param1=value3&param2=value4&param2=value5&param3=value6", result);
	}
}
