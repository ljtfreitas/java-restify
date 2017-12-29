/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.http.contract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.Form.Field;

public class FormObjectParameterSerializerTest {
	
	private FormObjectParameterSerializer serializer;

	@Before
	public void setup() {
		serializer = new FormObjectParameterSerializer();
	}

	@Test
	public void shouldSerializeFormObjectToQueryParametersFormat() {
		MyFormObject myFormObject = new MyFormObject();
		myFormObject.param1 = "value1";
		myFormObject.param2 = "value2";

		String result = serializer.serialize("name", MyFormObject.class, myFormObject);

		assertEquals("param1=value1&customParamName=value2", result);
	}

	@Test
	public void shouldReturnNullWhenFormObjectSourceIsNull() {
		MyFormObject myFormObject = null;

		String result = serializer.serialize("name", MyFormObject.class, myFormObject);

		assertNull(result);
	}

	@Form
	private class MyFormObject {

		@Field
		private String param1;

		@Field("customParamName")
		private String param2;
	}
}
