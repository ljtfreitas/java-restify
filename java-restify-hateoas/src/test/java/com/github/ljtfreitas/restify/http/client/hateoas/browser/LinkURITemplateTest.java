package com.github.ljtfreitas.restify.http.client.hateoas.browser;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.client.hateoas.Resource;

public class LinkURITemplateTest {

	@Test
	public void shouldNotModifySourceUriWhenHasNoParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path");

		assertEquals("http://my.api.com/path", linkURITemplate.expand().toString());
	}

	@Test
	public void shouldExpandUriTemplateWithSimpleStringVariableUsingResourceParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path/{name}");

		Resource<Person> resource = new Resource<Person>(new Person("ljtfreitas"));

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(resource).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithSimpleStringVariableUsingMapParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path/{name}");

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithSimpleStringVariableUsingLinkURITemplateParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path/{name}");

		LinkURITemplateParameters parameters = new LinkURITemplateParameters();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithReservedVariableUsingResourceParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path/{+name}");

		Resource<Person> resource = new Resource<Person>(new Person("ljtfreitas"));

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(resource).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithReservedVariableUsingMapParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path/{+name}");

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithReservedVariableUsingLinkURITemplateParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path/{+name}");

		LinkURITemplateParameters parameters = new LinkURITemplateParameters();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithFragmentVariableUsingResourceParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{#name}");

		Resource<Person> resource = new Resource<Person>(new Person("ljtfreitas"));

		assertEquals("http://my.api.com/path#ljtfreitas", linkURITemplate.expand(resource).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithFragmentVariableUsingMapParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{#name}");

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path#ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithFragmentVariableUsingLinkURITemplateParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{#name}");

		LinkURITemplateParameters parameters = new LinkURITemplateParameters();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path#ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithSinpleStringVariableUsingResourceParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{/name}");

		Resource<Person> resource = new Resource<Person>(new Person("ljtfreitas"));

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(resource).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithPathSegmentVariableUsingMapParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{/name}");

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithPathSegmentVariableUsingLinkURITemplateParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{/name}");

		LinkURITemplateParameters parameters = new LinkURITemplateParameters();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithQueryParameterVariableUsingResourceParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{?name}");

		Resource<Person> resource = new Resource<Person>(new Person("ljtfreitas"));

		assertEquals("http://my.api.com/path?name=ljtfreitas", linkURITemplate.expand(resource).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithQueryParameterVariableUsingMapParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{?name}");

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path?name=ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithQueryParameterVariableUsingLinkURITemplateParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{?name}");

		LinkURITemplateParameters parameters = new LinkURITemplateParameters();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path?name=ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithQueryParameterContinuedVariableUsingResourceParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path?age=32{&name}");

		Resource<Person> resource = new Resource<Person>(new Person("ljtfreitas"));

		assertEquals("http://my.api.com/path?age=32&name=ljtfreitas", linkURITemplate.expand(resource).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithQueryParameterContinuedVariableUsingMapParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path?age=32{&name}");

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path?age=32&name=ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithQueryParameterContinuedVariableUsingLinkURITemplateParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path?age=32{&name}");

		LinkURITemplateParameters parameters = new LinkURITemplateParameters();
		parameters.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path?age=32&name=ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	private class Person {

		@SuppressWarnings("unused")
		String name;

		Person(String name) {
			this.name = name;
		}
	}
}
