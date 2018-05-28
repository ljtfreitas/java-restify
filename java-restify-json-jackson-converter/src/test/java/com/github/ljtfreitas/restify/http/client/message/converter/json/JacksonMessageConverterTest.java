package com.github.ljtfreitas.restify.http.client.message.converter.json;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;
import com.github.ljtfreitas.restify.http.client.message.request.RequestBody;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class JacksonMessageConverterTest {

	@Mock
	private HttpRequestMessage request;

	@Mock
	private HttpResponseMessage response;

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
		
		when(request.charset()).thenReturn(Encoding.UTF_8.charset());
	}

	@Test
	public void shouldWriteJsonMessage() {
		RequestBody output = new RequestBody();

		when(request.body()).thenReturn(output);
		
		converter.write(new MyJsonModel("Tiago de Freitas Lima", 31), request);

		assertEquals(json, output.toString());
	}

	@Test
	public void shouldWriteCollectionOfJsonMessage() {
		RequestBody output = new RequestBody();

		when(request.body()).thenReturn(output);
		
		Collection<MyJsonModel> collection = Arrays.asList(new MyJsonModel("Tiago de Freitas Lima 1", 31), new MyJsonModel("Tiago de Freitas Lima 2", 32));

		converter.write(collection, request);

		assertEquals(collectionOfJson, output.toString());
	}

	@Test
	public void shouldReadJsonMessage() {
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes());

		when(response.body()).thenReturn(input);

		MyJsonModel myJsonModel = (MyJsonModel) converter.read(response, MyJsonModel.class);

		assertEquals("Tiago de Freitas Lima", myJsonModel.name);
		assertEquals(31, myJsonModel.age);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReadJsonMessageToDefaultJacksonObject() {
		ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes());

		when(response.body()).thenReturn(input);
		
		Map<Object, Object> map = (Map<Object, Object>) converter.read(response, Object.class);

		assertEquals("Tiago de Freitas Lima", map.get("name"));
		assertEquals(Integer.valueOf(31), Integer.valueOf(map.get("age").toString()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReadCollectionOfJsonMessage() throws Exception {
		ByteArrayInputStream input = new ByteArrayInputStream(collectionOfJson.getBytes());

		when(response.body()).thenReturn(input);

		Type genericCollectionType = new SimpleParameterizedType(Collection.class, null, MyJsonModel.class);

		Collection<MyJsonModel> collectionOfMyJsonModel = (Collection<MyJsonModel>) converter.read(response, genericCollectionType);

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
