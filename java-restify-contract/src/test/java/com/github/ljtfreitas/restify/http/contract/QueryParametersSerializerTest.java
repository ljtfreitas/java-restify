package com.github.ljtfreitas.restify.http.contract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class QueryParametersSerializerTest {

	private QueryParametersSerializer serializer = new QueryParametersSerializer();

	@Test
	public void shouldSerializeParametersObjectToQueryParametersFormat() {
		Parameters parameters = new Parameters()
				.put("param1", "value1")
				.put("param1", "value2")
				.put("param2", "value3");

		String result = serializer.serialize("", Parameters.class, parameters);

		assertEquals("param1=value1&param1=value2&param2=value3", result);
	}

	@Test
	public void shouldSerializeMapToQueryParametersFormat() {
		Map<String, Object> parameters = new LinkedHashMap<>();
		parameters.put("param1", "value1");
		parameters.put("param2", "value2");
		parameters.put("param3", "value3");

		String result = serializer.serialize("", Map.class, parameters);

		assertEquals("param1=value1&param2=value2&param3=value3", result);
	}

	@Test
	public void shouldSerializeMapOfIterableToQueryParametersFormat() {
		Map<String, Collection<String>> parameters = new LinkedHashMap<>();
		parameters.put("param1", Arrays.asList("value1", "value2", "value3"));
		parameters.put("param2", Arrays.asList("value4", "value5"));
		parameters.put("param3", Arrays.asList("value6"));

		String result = serializer.serialize("", Map.class, parameters);

		assertEquals("param1=value1&param1=value2&param1=value3&param2=value4&param2=value5&param3=value6", result);
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowExceptionWhenMapHasKeyTypeDifferentOfString() {
		Map<Integer, String> parameters = new LinkedHashMap<>();
		parameters.put(0, "zero");
		parameters.put(1, "one");

		ParameterizedType parameterizedMapType = new SimpleParameterizedType(Map.class, null,
				new Type[] { Integer.class, String.class });

		serializer.serialize("", parameterizedMapType, parameters);
	}

	@Test
	public void shouldReturNullWhenParameterSourceIsNull() {
		String result = serializer.serialize("", Parameters.class, null);

		assertNull(result);
	}
}
