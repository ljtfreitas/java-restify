package com.github.ljtfreitas.restify.http.client.hateoas.hal;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.hateoas.Resource;
import com.github.ljtfreitas.restify.http.client.message.response.HttpResponseMessage;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class JacksonHypermediaHalJsonMessageConverterTest {

	@Mock
	private HttpResponseMessage response;

	private JacksonHypermediaHalJsonMessageConverter<Resource<? extends Object>> jsonConverter;

	@Before
	public void setup() {
		jsonConverter = new JacksonHypermediaHalJsonMessageConverter<>();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldSendGetRequestWithHalLinks() {
		String json = "{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\"," + "\"_links\":{"
			+ "\"self\":{\"href\":\"http://localhost:7080/\"},"
			+ "\"friends\":{\"href\":\"http://localhost:7080/{user}/friends\",\"templated\":true}}}";

		when(response.body()).thenReturn(new ByteArrayInputStream(json.getBytes()));

		Resource<MyModel> resource = (Resource<MyModel>) jsonConverter.read(response, JavaType.parameterizedType(Resource.class, MyModel.class));

		MyModel myModel = resource.content();

		assertEquals("Tiago de Freitas Lima", myModel.name);
		assertEquals("1985-07-02", myModel.birthDate);

		assertEquals(2, resource.links().size());

		Optional<Link> self = resource.links().self();
		assertTrue(self.isPresent());
		assertEquals("http://localhost:7080/", self.get().href());

		Optional<Link> friends = resource.links().get("friends");
		assertTrue(friends.isPresent());
		assertEquals("http://localhost:7080/{user}/friends", friends.get().href());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldGetEmbeddedFieldOnTheResponse() {
		String json = "{\"name\":\"Tiago de Freitas Lima\",\"username\":\"ljtfreitas\","
			+ "\"_links\":{\"self\":{\"href\":\"http://localhost:7080/\"}},"
			+ "\"_embedded\":{"
				+ "\"friends\":[{\"name\":\"Fulano de Tal\",\"_links\":{\"self\":{\"href\":\"http://my.api/fulano\"}}}]"
				+ ",\"book\":{\"title\":\"1984\",\"_links\":{\"self\":{\"href\":\"http://my.api/books/1984\"}}}"
			+ "}}";

		when(response.body()).thenReturn(new ByteArrayInputStream(json.getBytes()));
		
		Resource<User> resource = (Resource<User>) jsonConverter.read(response, JavaType.parameterizedType(Resource.class, User.class));

		User user = resource.content();

		assertEquals("Tiago de Freitas Lima", user.name);
		assertEquals("ljtfreitas", user.username);

		Collection<Resource<User>> friends = resource.embedded().field("friends").get().collectionOf(User.class);
		assertThat(friends, hasSize(1));
		Optional<Link> firstFriendSelfLink = friends.iterator().next().links().get("self");
		assertTrue(firstFriendSelfLink.isPresent());
		assertEquals("http://my.api/fulano", firstFriendSelfLink.get().href());

		Resource<Book> book = resource.embedded().field("book").get().as(Book.class);
		assertEquals("1984", book.content().title);
		Optional<Link> bookSelfLink = book.links().get("self");
		assertTrue(bookSelfLink.isPresent());
		assertEquals("http://my.api/books/1984", bookSelfLink.get().href());
	}

	private static class MyModel {

		@JsonProperty
		String name;

		@JsonProperty("birth_date")
		String birthDate;

		private MyModel(@JsonProperty("name") String name, @JsonProperty("birth_date") String birthDate) {
			this.name = name;
			this.birthDate = birthDate;
		}
	}

	private static class User {

		private String name;

		private String username;

		private User(@JsonProperty("name") String name, @JsonProperty("username") String username) {
			this.name = name;
			this.username = username;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof User)) return false;

			User that = (User) obj;
			return name.equals(that.name) && username.equals(that.username);
		}
	}

	private static class Book {

		@JsonProperty
		String title;

		private Book(@JsonProperty("title") String title) {
			this.title = title;
		}
	}
}
