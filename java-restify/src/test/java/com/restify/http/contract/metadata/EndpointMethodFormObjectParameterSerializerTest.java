package com.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.contract.Form;
import com.restify.http.contract.Form.Field;
import com.restify.http.contract.metadata.EndpointMethodFormObjectParameterSerializer;

public class EndpointMethodFormObjectParameterSerializerTest {

	private EndpointMethodFormObjectParameterSerializer serializer;

	@Before
	public void setup() {
		serializer = new EndpointMethodFormObjectParameterSerializer();
	}

	@Test
	public void shouldSerializeFormObjectToQueryParametersFormat() {
		MyFormObject myFormObject = new MyFormObject();
		myFormObject.param1 = "value1";
		myFormObject.param2 = "value2";

		String result = serializer.serialize("name", MyFormObject.class, myFormObject);

		assertEquals("param1=value1&customParamName=value2", result);
	}

	@Form
	private class MyFormObject {

		@Field
		private String param1;

		@Field("customParamName")
		private String param2;
	}
}
