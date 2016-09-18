package com.restify.http.client.converter.json;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.converter.SimpleHttpRequestMessage;
import com.restify.http.client.converter.SimpleHttpResponseMessage;
import com.restify.http.metadata.reflection.SimpleParameterizedType;

public class JacksonMessageConverterTest {

	private JacksonMessageConverter<Object> converter = new JacksonMessageConverter<>();

	private String json;

	private String collectionOfJson;

	@Before
	public void setup() {
		StringBuilder sb = new StringBuilder();
		sb.append("{")
			.append("\"name\":\"Tiago de Freitas Lima\",")
			.append("\"age\":31")
			.append("}");

		json = sb.toString();

		sb = new StringBuilder();
		sb.append("[")
			.append("{")
				.append("\"name\":\"Tiago de Freitas Lima 1\",")
				.append("\"age\":31")
			.append("}")
			.append(",")
			.append("{")
				.append("\"name\":\"Tiago de Freitas Lima 2\",")
				.append("\"age\":32")
			.append("}")
		.append("]");

		collectionOfJson = sb.toString();
	}

	@Test
	public void shouldWriteJsonMessage() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		converter.write(new MyJsonModel("Tiago de Freitas Lima", 31), new SimpleHttpRequestMessage(output));

		assertEquals(json, output.toString());
	}

	@Test
	public void shouldWriteCollectionOfJsonMessage() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		Collection<MyJsonModel> collection = Arrays.asList(new MyJsonModel("Tiago de Freitas Lima 1", 31), new MyJsonModel("Tiago de Freitas Lima 2", 32));

		converter.write(collection, new SimpleHttpRequestMessage(output));

		assertEquals(collectionOfJson, output.toString());
	}

	@Test
	public void shouldReadJsonMessage() {
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes());

		MyJsonModel myJsonModel = (MyJsonModel) converter.read(new SimpleHttpResponseMessage(input), MyJsonModel.class);

		assertEquals("Tiago de Freitas Lima", myJsonModel.name);
		assertEquals(31, myJsonModel.age);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReadCollectionOfJsonMessage() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(collectionOfJson.getBytes());

		Type genericCollectionType = new SimpleParameterizedType(Collection.class, null, MyJsonModel.class);

		Collection<MyJsonModel> collectionOfMyJsonModel = (Collection<MyJsonModel>) converter.read(new SimpleHttpResponseMessage(input), genericCollectionType);

		assertEquals(2, collectionOfMyJsonModel.size());

		Iterator<MyJsonModel> iterator = collectionOfMyJsonModel.iterator();

		MyJsonModel myJsonModel = iterator.next();
		assertEquals("Tiago de Freitas Lima 1", myJsonModel.name);
		assertEquals(31, myJsonModel.age);

		myJsonModel = iterator.next();
		assertEquals("Tiago de Freitas Lima 2", myJsonModel.name);
		assertEquals(32, myJsonModel.age);
	}
}
