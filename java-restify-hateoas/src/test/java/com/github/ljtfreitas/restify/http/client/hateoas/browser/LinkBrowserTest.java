package com.github.ljtfreitas.restify.http.client.hateoas.browser;

import static com.github.ljtfreitas.restify.http.client.hateoas.browser.Hop.rel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import java.util.Collection;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.request.interceptor.EndpointRequestInterceptorStack;

@RunWith(MockitoJUnitRunner.class)
public class LinkBrowserTest {

	@Mock
	private EndpointRequestInterceptorStack endpointRequestInterceptorStack;

	@Rule
	public MockServerRule mockServerRule = new MockServerRule(this, 7080);

	private MockServerClient mockServerClient;

	private LinkBrowser linkBrowser;

	@Before
	public void setup() {
		when(endpointRequestInterceptorStack.apply(any())).then(returnsFirstArg());

		linkBrowser = new LinkBrowserBuilder().build();

		mockServerClient = new MockServerClient("localhost", 7080);
	}

	@Test
	public void shouldFollowSingleLink() {
		HttpRequest httpRequest = request()
			.withMethod("GET")
			.withPath("/me");

		mockServerClient.when(httpRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/hal+json")
					.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\"}")));

		Person person = linkBrowser
			.follow(Link.self("http://localhost:7080/me"))
				.as(Person.class);

		assertNotNull(person);

		mockServerClient.verify(httpRequest);
	}

	@Test
	public void shouldFollowHalLinkWithResponseOfCollectionType() {
		HttpRequest personRequest = request()
			.withMethod("GET")
			.withPath("/me");

		HttpRequest friendsRequest = request()
			.withMethod("GET")
			.withPath("/tiago/friends");

		mockServerClient.when(personRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/hal+json")
					.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\","
								+ "\"_links\":{"
									+ "\"self\":{\"href\":\"http://localhost:8080/\"},"
									+ "\"friends\":{\"href\":\"http://localhost:7080/{user}/friends\",\"templated\":true}"
								+ "}}")));

		mockServerClient.when(friendsRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/hal+json")
					.withBody(json("[{\"name\":\"Fulano de Tal\",\"birth_date\":\"1985-08-02\"},"
								  + "{\"name\":\"Beltrano da Silva\",\"birth_date\":\"1985-09-02\"},"
								  + "{\"name\":\"Sicrano dos Santos\",\"birth_date\":\"1985-10-02\"}"
								 + "]")));

		Collection<Person> friends = linkBrowser
				.follow(Link.self("http://localhost:7080/me"))
					.follow(rel("friends").parameter("user", "tiago"))
						.collectionOf(Person.class);

		assertThat(friends, Matchers.hasSize(3));
		assertThat(friends, Matchers.hasItem(new Person("Fulano de Tal", "1985-08-02")));
		assertThat(friends, Matchers.hasItem(new Person("Beltrano da Silva", "1985-09-02")));
		assertThat(friends, Matchers.hasItem(new Person("Sicrano dos Santos", "1985-10-02")));

		mockServerClient.verify(personRequest, friendsRequest);
	}

	@Test
	public void shouldFollowWebLinkWithResponseOfCollectionType() {
		HttpRequest personRequest = request()
			.withMethod("GET")
			.withPath("/me");

		HttpRequest friendsRequest = request()
			.withMethod("GET")
			.withPath("/tiago/friends");

		mockServerClient.when(personRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\","
								+ "\"links\":["
									+ "{\"rel\":\"self\",\"href\":\"http://localhost:8080/\"},"
									+ "{\"rel\":\"friends\",\"href\":\"http://localhost:7080/{user}/friends\",\"templated\":true}"
								+ "]}")));

		mockServerClient.when(friendsRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("[{\"name\":\"Fulano de Tal\",\"birth_date\":\"1985-08-02\"},"
								  + "{\"name\":\"Beltrano da Silva\",\"birth_date\":\"1985-09-02\"},"
								  + "{\"name\":\"Sicrano dos Santos\",\"birth_date\":\"1985-10-02\"}"
								 + "]")));

		Collection<Person> friends = linkBrowser
				.follow(Link.self("http://localhost:7080/me"))
					.follow(rel("friends").parameter("user", "tiago"))
						.collectionOf(Person.class);

		assertThat(friends, Matchers.hasSize(3));
		assertThat(friends, Matchers.hasItem(new Person("Fulano de Tal", "1985-08-02")));
		assertThat(friends, Matchers.hasItem(new Person("Beltrano da Silva", "1985-09-02")));
		assertThat(friends, Matchers.hasItem(new Person("Sicrano dos Santos", "1985-10-02")));

		mockServerClient.verify(personRequest, friendsRequest);
	}

	@Test
	public void shouldFollowMultipleHalLinks() {
		HttpRequest personRequest = request()
			.withMethod("GET")
			.withPath("/me");

		HttpRequest friendsRequest = request()
			.withMethod("GET")
			.withPath("/tiago/friends");

		HttpRequest cityRequest = request()
			.withMethod("GET")
			.withPath("/cities/rio-de-janeiro");

		mockServerClient.when(personRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/hal+json")
					.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\","
								+ "\"_links\":{"
									+ "\"self\":{\"href\":\"http://localhost:8080/\"},"
									+ "\"friends\":{\"href\":\"http://localhost:7080/{user}/friends\",\"templated\":true}"
								+ "}}")));

		mockServerClient.when(friendsRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/hal+json")
					.withBody(json("["
								+ "{\"name\":\"Fulano de Tal\",\"birth_date\":\"1985-08-02\","
									+ "\"_links\":{\"city\":{\"href\":\"http://localhost:7080/cities/sao-paulo\"}}},"
								+ "{\"name\":\"Beltrano da Silva\",\"birth_date\":\"1985-09-02\","
									+ "\"_links\":{\"city\":{\"href\":\"http://localhost:7080/cities/rio-de-janeiro\"}}}"
								+ "]")));

		mockServerClient.when(cityRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\":\"Rio de Janeiro\",\"state\":\"Rio de Janeiro\"}")));

		City city = linkBrowser
				.follow(Link.self("http://localhost:7080/me"))
					.follow(rel("friends").parameter("user", "tiago"))
						.follow(rel("$.[1]._links.city.href"))
							.as(City.class);

		assertEquals("Rio de Janeiro", city.name);
		assertEquals("Rio de Janeiro", city.state);

		mockServerClient.verify(personRequest, friendsRequest, cityRequest);
	}

	@Test
	public void shouldFollowMultipleWebLinks() {
		HttpRequest personRequest = request()
			.withMethod("GET")
			.withPath("/me");

		HttpRequest friendsRequest = request()
			.withMethod("GET")
			.withPath("/tiago/friends");

		HttpRequest cityRequest = request()
			.withMethod("GET")
			.withPath("/cities/sao-paulo");

		mockServerClient.when(personRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\","
								+ "\"links\":["
									+ "{\"rel\":\"self\",\"href\":\"http://localhost:8080/\"},"
									+ "{\"rel\":\"friends\",\"href\":\"http://localhost:7080/{user}/friends\",\"templated\":true}"
								+ "]}")));

		mockServerClient.when(friendsRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("[{\"name\":\"Fulano de Tal\",\"birth_date\":\"1985-08-02\","
								+ "\"links\":["
									+ "{\"rel\":\"city\",\"href\":\"http://localhost:7080/cities/sao-paulo\"}"
									+ "]"
								+ "}]")));

		mockServerClient.when(cityRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\":\"Sao Paulo\",\"state\":\"Sao Paulo\"}")));

		City city = linkBrowser
				.follow(Link.self("http://localhost:7080/me"))
					.follow(rel("friends").parameter("user", "tiago"))
						.follow(rel("$.[0].links[?(@.rel == 'city')].href"))
							.as(City.class);

		assertEquals("Sao Paulo", city.name);
		assertEquals("Sao Paulo", city.state);

		mockServerClient.verify(personRequest, friendsRequest, cityRequest);
	}

	@Test
	public void shouldFollowLinksOnPropertiesUsingJsonPathExpression() {
		HttpRequest personRequest = request()
			.withMethod("GET")
			.withPath("/me");

		HttpRequest friendsRequest = request()
			.withMethod("GET")
			.withPath("/tiago/friends");

		HttpRequest cityRequest = request()
			.withMethod("GET")
			.withPath("/cities/sao-paulo");

		mockServerClient.when(personRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\","
								+ "\"friends_url\":\"http://localhost:7080/tiago/friends\"}")));

		mockServerClient.when(friendsRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("[{\"name\":\"Fulano de Tal\",\"birth_date\":\"1985-08-02\","
								+ "\"city_url\":\"http://localhost:7080/cities/sao-paulo\"}]")));

		mockServerClient.when(cityRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\":\"Sao Paulo\",\"state\":\"Sao Paulo\"}")));

		City city = linkBrowser
				.follow(Link.self("http://localhost:7080/me"))
					.follow("$.friends_url", "$.[0].city_url")
						.as(City.class);

		assertEquals("Sao Paulo", city.name);
		assertEquals("Sao Paulo", city.state);

		mockServerClient.verify(personRequest, friendsRequest, cityRequest);
	}

	@Test
	public void shouldFollowLinksOnPropertiesUsingRootJsonPath() {
		HttpRequest personRequest = request()
			.withMethod("GET")
			.withPath("/me");

		HttpRequest wifeRequest = request()
			.withMethod("GET")
			.withPath("/tatiana-gomes-da-silva");

		HttpRequest cityRequest = request()
			.withMethod("GET")
			.withPath("/cities/sao-paulo");

		mockServerClient.when(personRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\","
								+ "\"wife_url\":\"http://localhost:7080/tatiana-gomes-da-silva\"}")));

		mockServerClient.when(wifeRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\":\"Tatiana Gomes da Silva\",\"birth_date\":\"1983-10-05\","
								+ "\"city_url\":\"http://localhost:7080/cities/sao-paulo\"}")));

		mockServerClient.when(cityRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\":\"Sao Paulo\",\"state\":\"Sao Paulo\"}")));

		linkBrowser = new LinkBrowserBuilder()
				.discovery()
					.jsonPath()
					.and()
				.build();

		City city = linkBrowser
				.follow(Link.self("http://localhost:7080/me"))
					.follow("wife_url", "city_url")
						.as(City.class);

		assertEquals("Sao Paulo", city.name);
		assertEquals("Sao Paulo", city.state);

		mockServerClient.verify(personRequest, wifeRequest, cityRequest);
	}

	@Test
	public void shouldFollowLinksUsingBaseUrlWhenIPresent() {
		HttpRequest personRequest = request()
			.withMethod("GET")
			.withPath("/me");

		HttpRequest wifeRequest = request()
			.withMethod("GET")
			.withPath("/tatiana-gomes-da-silva");

		mockServerClient.when(personRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/hal+json")
					.withBody(json("{\"name\":\"Tiago de Freitas Lima\",\"birth_date\":\"1985-07-02\","
								+ "\"_links\":{\"wife\":{\"href\":\"/tatiana-gomes-da-silva\"}}}")));

		mockServerClient.when(wifeRequest)
			.respond(
				response()
					.withStatusCode(200)
					.withHeader("Content-Type", "application/json")
					.withBody(json("{\"name\":\"Tatiana Gomes da Silva\",\"birth_date\":\"1983-10-05\"}")));

		linkBrowser = new LinkBrowserBuilder().baseURL("http://localhost:7080").build();

		Person wife = linkBrowser
				.follow(Link.self("http://localhost:7080/me"))
					.follow("wife")
						.as(Person.class);

		assertEquals("Tatiana Gomes da Silva", wife.name);

		mockServerClient.verify(personRequest, wifeRequest);
	}

	private static class Person {

		private final String name;
		private final String birthDate;

		Person(@JsonProperty("name") String name, @JsonProperty("birth_date") String birthDate) {
			this.name = name;
			this.birthDate = birthDate;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Person) {
				Person that = (Person) obj;
				return this.name.equals(that.name) && this.birthDate.equals(that.birthDate);

			} else return false;
		}
	}

	private static class City {

		private final String name;
		private final String state;

		@JsonCreator
		private City(@JsonProperty("name") String name, @JsonProperty("state") String state) {
			this.name = name;
			this.state = state;
		}
	}
}
