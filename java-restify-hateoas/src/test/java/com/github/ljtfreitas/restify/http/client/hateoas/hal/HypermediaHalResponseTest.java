package com.github.ljtfreitas.restify.http.client.hateoas.hal;

import static com.github.ljtfreitas.restify.http.client.hateoas.browser.LinkURITemplateParameter.using;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.RestifyProxyBuilder;
import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.hateoas.Resource;
import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Path;

public class HypermediaHalResponseTest {

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MyApi myApi;

	private MockServerClient mockServerClient;

	@Before
	public void setup() {
		mockServerClient = new MockServerClient("localhost", 7080);

		JacksonHypermediaHalJsonMessageConverter<Object> jacksonMessageConverter = new JacksonHypermediaHalJsonMessageConverter<>();

		myApi = new RestifyProxyBuilder().converters(jacksonMessageConverter)
				.target(MyApi.class, "http://localhost:7080").build();
	}

	@Test
	public void shouldSendGetRequestWithHalLinks() {
		mockServerClient.when(request().withMethod("GET").withPath("/json")).respond(response().withStatusCode(200)
				.withHeader("Content-Type", "application/hal+json")
				.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\"," + "\"_links\":{"
						+ "\"self\":{\"href\":\"http://localhost:7080/\"},"
						+ "\"friends\":{\"href\":\"http://localhost:7080/{user}/friends\",\"templated\":true}}}")));

		Resource<MyModel> resource = myApi.json();

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

	@Test
	public void shouldFollowLinksOfTheResponse() {
		HttpRequest meRequest = request().withMethod("GET").withPath("/me");
		HttpRequest friendsRequest = request().withMethod("GET").withPath("/ljtfreitas/friends");

		mockServerClient.when(meRequest)
			.respond(response()
				.withStatusCode(200)
				.withHeader("Content-Type", "application/hal+json")
				.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"username\":\"ljtfreitas\","
							+ "\"_links\":{"
								+ "\"self\":{\"href\":\"http://localhost:7080/\"},"
								+ "\"friends\":{\"href\":\"http://localhost:7080/{username}/friends\",\"templated\":true}"
								+ "}"
							+ "}")));

		mockServerClient.when(friendsRequest)
			.respond(response()
				.withStatusCode(200)
				.withHeader("Content-Type", "application/json")
				.withBody(json("[{\"name\":\"Fulano de Tal\",\"username\":\"fulano\"}]")));

		Resource<User> resource = myApi.me();

		User user = resource.content();

		assertEquals("Tiago de Freitas Lima", user.name);
		assertEquals("ljtfreitas", user.username);

		Collection<User> friends = resource.links().get("friends").get()
				.follow(using("username", "ljtfreitas"))
					.asCollectionOf(User.class);

		assertThat(friends, hasItem(new User("Fulano de Tal", "fulano")));

		mockServerClient.verify(meRequest, friendsRequest);
	}


	@Test
	public void shouldGetEmbeddedFieldOnTheResponse() {
		HttpRequest meRequest = request().withMethod("GET").withPath("/me");

		mockServerClient.when(meRequest)
			.respond(response()
				.withStatusCode(200)
				.withHeader("Content-Type", "application/hal+json")
				.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"username\":\"ljtfreitas\","
							+ "\"_links\":{\"self\":{\"href\":\"http://localhost:7080/\"}},"
							+ "\"_embedded\":{"
								+ "\"friends\":[{\"name\":\"Fulano de Tal\",\"_links\":{\"self\":{\"href\":\"http://my.api/fulano\"}}}]"
								+ ",\"book\":{\"title\":\"1984\",\"_links\":{\"self\":{\"href\":\"http://my.api/books/1984\"}}}"
							+ "}}")));

		Resource<User> resource = myApi.me();

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

	interface MyApi {

		@Path("/json")
		@Get
		public Resource<MyModel> json();

		@Path("/me")
		@Get
		public Resource<User> me();

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
