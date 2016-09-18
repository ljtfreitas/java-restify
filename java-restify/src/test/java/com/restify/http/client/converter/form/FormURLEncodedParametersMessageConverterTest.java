package com.restify.http.client.converter.form;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.converter.SimpleHttpRequestMessage;
import com.restify.http.client.converter.SimpleHttpResponseMessage;
import com.restify.http.metadata.Parameters;

public class FormURLEncodedParametersMessageConverterTest {

	private FormURLEncodedParametersMessageConverter converter = new FormURLEncodedParametersMessageConverter();

	private String messageBody;

	@Before
	public void setup() {
		messageBody = "param1=value1&param2=value2";
	}

	@Test
	public void shouldWriteFormUrlEncodedMessageWithParametersSource() {
		Parameters body = new Parameters();
		body.put("param1", "value1");
		body.put("param2", "value2");

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		converter.write(body, new SimpleHttpRequestMessage(output));

		assertEquals(messageBody, output.toString());
	}

	@Test
	public void shouldReadFormUrlEncodedMessageWhenParametersIsExpectedType() {
		ByteArrayInputStream input = new ByteArrayInputStream(messageBody.getBytes());

		Parameters parameters = converter.read(new SimpleHttpResponseMessage(input), Parameters.class);

		assertEquals("value1", parameters.first("param1").get());
		assertEquals("value2", parameters.first("param2").get());
	}
}
