package com.github.ljtfreitas.restify.http.client.message.converter.form;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.request.BufferedHttpRequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.http.contract.Parameters;

@RunWith(MockitoJUnitRunner.class)
public class FormURLEncodedParametersMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private HttpResponseMessage response;
	
	private FormURLEncodedParametersMessageConverter converter = new FormURLEncodedParametersMessageConverter();

	private String messageBody;

	@Before
	public void setup() {
		messageBody = "param1=value1&param2=value2";

		when(request.charset()).thenReturn(Charset.forName("UTF-8"));
	}

	@Test
	public void shouldWriteFormUrlEncodedMessageWithParametersSource() {
		Parameters body = new Parameters()
			.put("param1", "value1")
			.put("param2", "value2");

		HttpRequestBody output = new BufferedHttpRequestBody();

		when(request.body()).thenReturn(output);
		
		converter.write(body, request);

		assertEquals(messageBody, output.asString());
	}

	@Test
	public void shouldReadFormUrlEncodedMessageWhenParametersIsExpectedType() {
		ByteArrayInputStream input = new ByteArrayInputStream(messageBody.getBytes());

		when(response.body().input()).thenReturn(input);
		
		Parameters parameters = converter.read(response, Parameters.class);

		assertEquals("value1", parameters.first("param1").get());
		assertEquals("value2", parameters.first("param2").get());
	}
}
