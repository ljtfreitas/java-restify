package com.github.ljtfreitas.restify.http.client.message.converter.form;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.converter.form.FormURLEncodedMapMessageConverter;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class FormURLEncodedMapMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock
	private HttpResponseMessage response;
	
	private FormURLEncodedMapMessageConverter converter = new FormURLEncodedMapMessageConverter();

	private String messageBody;

	@Before
	public void setup() {
		messageBody = "param1=value1&param2=value2";
		
		when(request.charset()).thenReturn(Charset.forName("UTF-8"));
	}

	@Test
	public void shouldWriteFormUrlEncodedMessageWithMapSource() {
		Map<String, String> body = new LinkedHashMap<>();
		body.put("param1", "value1");
		body.put("param2", "value2");

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		when(request.output()).thenReturn(output);

		converter.write(body, request);

		assertEquals(messageBody, output.toString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldThrowUnsupportedOperationExceptionOnTryReadMessage() {
		ByteArrayInputStream input = new ByteArrayInputStream(messageBody.getBytes());

		when(response.body()).thenReturn(input);
		
		converter.read(response, Map.class);
	}
}
