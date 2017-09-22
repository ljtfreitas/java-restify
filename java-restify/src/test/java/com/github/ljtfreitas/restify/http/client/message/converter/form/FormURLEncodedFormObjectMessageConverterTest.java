package com.github.ljtfreitas.restify.http.client.message.converter.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedFormObjectMessageConverter;
import com.github.ljtfreitas.restify.http.client.request.SimpleHttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.client.response.SimpleHttpResponseMessage;
import com.github.ljtfreitas.restify.http.contract.Form;
import com.github.ljtfreitas.restify.http.contract.Form.Field;

public class FormURLEncodedFormObjectMessageConverterTest {

	private FormURLEncodedFormObjectMessageConverter converter = new FormURLEncodedFormObjectMessageConverter();

	@Test
	public void shouldCanReadFormObjectType() {
		assertTrue(converter.canRead(MyFormObject.class));
	}

	@Test
	public void shouldCanWriteFormObjectType() {
		assertTrue(converter.canWrite(MyFormObject.class));
	}

	@Test
	public void shouldReadFormUrlEncodedMessageToFormObject() {
		String source = "name=Tiago de Freitas&customFieldName=31";

		HttpResponseMessage response = new SimpleHttpResponseMessage(new ByteArrayInputStream(source.getBytes()));

		MyFormObject myFormObject = (MyFormObject) converter.read(response, MyFormObject.class);

		assertEquals("Tiago de Freitas", myFormObject.name);
		assertEquals(31, myFormObject.age);
	}

	@Test
	public void shouldWriteFormUrlEncodedMessageWithFormObjectSource() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		SimpleHttpRequestMessage request = new SimpleHttpRequestMessage(output);

		MyFormObject myFormObject = new MyFormObject();
		myFormObject.name = "Tiago de Freitas";
		myFormObject.age = 31;

		converter.write(myFormObject, request);

		assertEquals("name=Tiago+de+Freitas&customFieldName=31", output.toString());
	}

	@Form
	static class MyFormObject {

		@Field
		private String name;

		@Field("customFieldName")
		private int age;
	}
}
