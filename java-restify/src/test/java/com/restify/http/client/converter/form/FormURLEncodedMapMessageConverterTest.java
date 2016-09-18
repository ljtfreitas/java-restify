package com.restify.http.client.converter.form;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.converter.SimpleHttpRequestMessage;
import com.restify.http.client.converter.SimpleHttpResponseMessage;

public class FormURLEncodedMapMessageConverterTest {

	private FormURLEncodedMapMessageConverter converter = new FormURLEncodedMapMessageConverter();

	private String messageBody;

	@Before
	public void setup() {
		messageBody = "param1=value1&param2=value2";
	}

	@Test
	public void shouldWriteFormUrlEncodedMessageWithMapSource() {
		Map<String, String> body = new LinkedHashMap<>();
		body.put("param1", "value1");
		body.put("param2", "value2");

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		converter.write(body, new SimpleHttpRequestMessage(output));

		assertEquals(messageBody, output.toString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldThrowUnsupportedOperationExceptionOnTryReadMessage() {
		ByteArrayInputStream input = new ByteArrayInputStream(messageBody.getBytes());

		converter.read(new SimpleHttpResponseMessage(input), Map.class);
	}
}
