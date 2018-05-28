package com.github.ljtfreitas.restify.http.client.message.converter.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;

@RunWith(MockitoJUnitRunner.class)
public class JaxbXmlMessageConverterTest {

	@Mock
	private HttpResponseMessage response;

	@Mock
	private HttpRequestMessage request;
	
	private JaxbXmlMessageConverter<MyXmlModel> converter = new JaxbXmlMessageConverter<>();

	private String xml;

	@Before
	public void setup() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
			.append("<model>")
			.append("<name>Tiago de Freitas Lima</name>")
			.append("<age>31</age>")
			.append("</model>");

		xml = sb.toString();
		
		when(request.charset()).thenReturn(Encoding.UTF_8.charset());
	}

	@Test
	public void shouldCanWriteTypeWithJaxbElementAnnotation() {
		assertTrue(converter.canRead(MyXmlModel.class));
	}

	@Test
	public void shouldNotCanWriteTypeWithoutJaxbElementAnnotation() {
		assertFalse(converter.canRead(NoJaxBType.class));
	}

	@Test
	public void shouldWriteXmlMessage() {
		RequestBody output = new RequestBody();

		when(request.body()).thenReturn(output);

		converter.write(new MyXmlModel("Tiago de Freitas Lima", 31), request);

		assertEquals(xml, output.toString());
	}

	@Test
	public void shouldCanReadTypeWithJaxbElementAnnotation() {
		assertTrue(converter.canRead(MyXmlModel.class));
	}

	@Test
	public void shouldNotCanReadTypeWithoutJaxbElementAnnotation() {
		assertFalse(converter.canRead(NoJaxBType.class));
	}

	@Test
	public void shouldReadXmlMessage() {
		ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());

		when(response.body()).thenReturn(input);

		MyXmlModel myXmlModel = converter.read(response, MyXmlModel.class);

		assertEquals("Tiago de Freitas Lima", myXmlModel.name);
		assertEquals(31, myXmlModel.age);
	}

	@XmlRootElement(name = "model")
	@XmlAccessorType(XmlAccessType.FIELD)
	static class MyXmlModel {

		String name;
		int age;

		MyXmlModel() {
		}

		MyXmlModel(String name, int age) {
			this.name = name;
			this.age = age;
		}
	}

	//Without @XmlRootElement
	static class NoJaxBType {
	}
}
