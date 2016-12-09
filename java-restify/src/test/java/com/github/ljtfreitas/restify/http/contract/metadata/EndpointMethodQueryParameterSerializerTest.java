package com.github.ljtfreitas.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class EndpointMethodQueryParameterSerializerTest {

	private EndpointMethodQueryParameterSerializer serializer = new EndpointMethodQueryParameterSerializer();

	@Test
	public void shouldSerializeCollectionToQueryParametersFormat() {
		Collection<String> parameters = new ArrayList<>();
		parameters.add("value1");
		parameters.add("value2");
		parameters.add("value3");

		String result = serializer.serialize("parameter", Collection.class, parameters);

		assertEquals("parameter=value1&parameter=value2&parameter=value3", result);
	}

	@Test
	public void shouldSerializeStringToQueryParametersFormat() {
		String value = "value1";

		String result = serializer.serialize("parameter", String.class, value);

		assertEquals("parameter=value1", result);
	}

	@Test
	public void shouldReturnNullWhenStringSourceIsNull() {
		String value = null;

		String result = serializer.serialize("parameter", String.class, value);

		assertNull(result);
	}

}
