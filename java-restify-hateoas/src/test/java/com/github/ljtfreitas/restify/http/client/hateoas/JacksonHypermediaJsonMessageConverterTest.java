package com.github.ljtfreitas.restify.http.client.hateoas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class JacksonHypermediaJsonMessageConverterTest {

	@Mock
	private HttpResponseMessage response;
	
	private JacksonHypermediaJsonMessageConverter<Resource<MyModel>> jsonConverter;

	@Before
	public void setup() {
		jsonConverter = new JacksonHypermediaJsonMessageConverter<>();
	}

	@Test
	public void shouldSendDeserializeJsonWithHalLinks() {
		String json = "{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\","
			+ "\"links\":["
				+ "{\"rel\":\"self\",\"href\":\"http://localhost:8080/\"},"
				+ "{\"rel\":\"friends\",\"href\":\"http://localhost:8080/{user}/friends\",\"templated\":true}"
			+ "]}";

		when(response.body()).thenReturn(new ByteArrayInputStream(json.getBytes()));

		Resource<MyModel> resource = jsonConverter.read(response, JavaType.parameterizedType(Resource.class, MyModel.class));

		MyModel myModel = resource.content();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals("1985-07-02", myModel.birthDate);

		assertEquals(2, resource.links().size());

		Optional<Link> self = resource.links().self();
		assertTrue(self.isPresent());
		assertEquals("http://localhost:8080/", self.get().href());

		Optional<Link> friends = resource.links().get("friends");
		assertTrue(friends.isPresent());
		assertEquals("http://localhost:8080/{user}/friends", friends.get().href());
	}

	public static class MyModel {

		@JsonProperty
		String name;

		@JsonProperty("birth_date")
		String birthDate;

		public MyModel(@JsonProperty("name") String name, @JsonProperty("birth_date") String birthDate) {
			this.name = name;
			this.birthDate = birthDate;
		}
	}
}
