package com.github.ljtfreitas.restify.http.client.message.converter.json;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class JsonBMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock
	private HttpResponseMessage response;

	private JsonBMessageConverter<Object> converter = new JsonBMessageConverter<>();

	private String json;

	private String collectionOfJson;

	@Before
	public void setup() {
		StringBuilder sb = new StringBuilder();
		sb.append("{")
			.append("\"age\":31,")
			.append("\"name\":\"Tiago de Freitas Lima\"")
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

		when(request.charset()).thenReturn(Charset.forName("UTF-8"));
	}

	@Test
	public void shouldWriteJsonMessage() {
		RequestBody output = new RequestBody();

		when(request.body()).thenReturn(output);

		converter.write(new MyJsonModel("Tiago de Freitas Lima", 31), request);

		assertEquals(json, output.toString());
	}

	@Test
	public void shouldReadJsonMessage() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes());

		when(response.body()).thenReturn(input);

		MyJsonModel myJsonModel = (MyJsonModel) converter.read(response, MyJsonModel.class);

		assertEquals("Tiago de Freitas Lima", myJsonModel.getName());
		assertEquals(31, myJsonModel.getAge());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReadCollectionOfJsonMessage() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(collectionOfJson.getBytes());

		when(response.body()).thenReturn(input);

		Type genericCollectionType = new SimpleParameterizedType(ArrayList.class, null, MyJsonModel.class);

		Collection<MyJsonModel> collectionOfMyJsonModel = (Collection<MyJsonModel>) converter.read(response,
				genericCollectionType);

		assertEquals(2, collectionOfMyJsonModel.size());

		Iterator<MyJsonModel> iterator = collectionOfMyJsonModel.iterator();

		MyJsonModel myJsonModel = iterator.next();
		assertEquals("Tiago de Freitas Lima 1", myJsonModel.getName());
		assertEquals(31, myJsonModel.getAge());

		myJsonModel = iterator.next();
		assertEquals("Tiago de Freitas Lima 2", myJsonModel.getName());
		assertEquals(32, myJsonModel.getAge());
	}

}
