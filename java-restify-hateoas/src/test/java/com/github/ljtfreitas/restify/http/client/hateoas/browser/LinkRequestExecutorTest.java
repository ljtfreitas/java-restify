package com.github.ljtfreitas.restify.http.client.hateoas.browser;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.hateoas.Resource;

public class LinkRequestExecutorTest {

	@Test
	public void test() {
		LinkRequestExecutor linkRequestExecutor = new LinkRequestExecutor(null, null);

		Resource<Person> resource = linkRequestExecutor
				.execute(new LinkEndpointRequest(Link.self("http://localhost:8080/me"), Person.class));

		Person person = resource.content();

		assertNotNull(person);
	}

	private static class Person {

		@JsonProperty
		private String name;

		@JsonProperty("birth_date")
		private String birthDate;
	}

}
