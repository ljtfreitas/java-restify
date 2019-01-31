package com.github.ljtfreitas.restify.http.client.message.converter.form;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.request.BufferedByteArrayHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class FormURLEncodedMapMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
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

		BufferedHttpRequestBody output = new BufferedByteArrayHttpRequestBody();

		when(request.body()).thenReturn(output);

		converter.write(body, request);

		assertEquals(messageBody, new String(output.asBytes()));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldThrowUnsupportedOperationExceptionOnTryReadMessage() {
		ByteArrayInputStream input = new ByteArrayInputStream(messageBody.getBytes());

		when(response.body().input()).thenReturn(input);
		
		converter.read(response, Map.class);
	}
}
