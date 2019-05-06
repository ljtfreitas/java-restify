package com.github.ljtfreitas.restify.http.client.message.converter.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.converter.form.provided.FormURLEncodedFormObjectMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedByteArrayHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.contract.form.Form;
import com.github.ljtfreitas.restify.http.contract.form.Form.Field;

@RunWith(MockitoJUnitRunner.class)
public class FormURLEncodedFormObjectMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private HttpResponseMessage response;

	private FormURLEncodedFormObjectMessageConverter converter = new FormURLEncodedFormObjectMessageConverter();

	@Before
	public void setup() {
		when(request.charset()).thenReturn(Charset.forName("UTF-8"));
	}

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

		when(response.body().input()).thenReturn(new ByteArrayInputStream(source.getBytes()));

		MyFormObject myFormObject = (MyFormObject) converter.read(response, MyFormObject.class);

		assertEquals("Tiago de Freitas", myFormObject.name);
		assertEquals(31, myFormObject.age);
	}

	@Test
	public void shouldWriteFormUrlEncodedMessageWithFormObjectSource() {
		BufferedHttpRequestBody output = new BufferedByteArrayHttpRequestBody();

		when(request.body()).thenReturn(output);

		MyFormObject myFormObject = new MyFormObject();
		myFormObject.name = "Tiago de Freitas";
		myFormObject.age = 31;

		converter.write(myFormObject, request);

		assertEquals("name=Tiago+de+Freitas&customFieldName=31", new String(output.asBytes()));
	}

	@Form
	static class MyFormObject {

		@Field
		private String name;

		@Field("customFieldName")
		private int age;
	}
}
