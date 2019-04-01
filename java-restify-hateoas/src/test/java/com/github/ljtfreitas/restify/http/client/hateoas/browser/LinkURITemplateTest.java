package com.github.ljtfreitas.restify.http.client.hateoas.browser;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class LinkURITemplateTest {

	@Test
	public void shouldNotModifySourceUriWhenHasNoParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path");

		assertEquals("http://my.api.com/path", linkURITemplate.expand().toString());
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

		LinkURITemplateParameters parameters = new LinkURITemplateParameters()
				.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithMultipleSimpleStringVariables() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path/{name,nickname}");

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("name", "tiago");
		parameters.put("nickname", "ljtfreitas");

		assertEquals("http://my.api.com/path/tiago,ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldDiscardSimpleStringVariableWhenParameterIsNotPresent() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path/{name}");

		assertEquals("http://my.api.com/path/", linkURITemplate.expand().toString());
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

		LinkURITemplateParameters parameters = new LinkURITemplateParameters()
				.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithMultipleReservedVariables() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path/{+name,nickname}");

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("name", "tiago");
		parameters.put("nickname", "ljtfreitas");

		assertEquals("http://my.api.com/path/tiago,ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldDiscardReservedVariableWhenParameterIsNotPresent() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path/{+name}");

		assertEquals("http://my.api.com/path/", linkURITemplate.expand().toString());
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

		LinkURITemplateParameters parameters = new LinkURITemplateParameters()
				.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path#ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithMultipleFragmentVariable() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{#name,nickname}");

		Map<String, String> parameters = new LinkedHashMap<>();
		parameters.put("name", "tiago");
		parameters.put("nickname", "ljtfreitas");

		assertEquals("http://my.api.com/path#tiago,ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldDiscardFragmentVariableWhenParameterIsNotPresent() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{#name}");

		assertEquals("http://my.api.com/path", linkURITemplate.expand().toString());
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

		LinkURITemplateParameters parameters = new LinkURITemplateParameters()
				.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path/ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithMultiplePathSegmentVariables() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{/name,nickname}");

		LinkURITemplateParameters parameters = new LinkURITemplateParameters()
				.put("name", "tiago")
				.put("nickname", "ljtfreitas");

		assertEquals("http://my.api.com/path/tiago/ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldDiscardPathSegmentVariableWhenParameterIsNotPresent() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{/name}");

		assertEquals("http://my.api.com/path", linkURITemplate.expand().toString());
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

		LinkURITemplateParameters parameters = new LinkURITemplateParameters()
				.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path?name=ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithMultipleQueryParameterVariables() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{?name,nickname}");

		LinkURITemplateParameters parameters = new LinkURITemplateParameters()
				.put("name", "tiago")
				.put("nickname", "ljtfreitas");

		assertEquals("http://my.api.com/path?name=tiago&nickname=ljtfreitas",
				linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldDiscardQueryParameterVariableWhenParameterIsNotPresent() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path{?name}");

		assertEquals("http://my.api.com/path", linkURITemplate.expand().toString());
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

		LinkURITemplateParameters parameters = new LinkURITemplateParameters()
				.put("name", "ljtfreitas");

		assertEquals("http://my.api.com/path?age=32&name=ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldExpandUriTemplateWithMultipleQueryParameters() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path?age=32{&name,nickname}");

		LinkURITemplateParameters parameters = new LinkURITemplateParameters()
				.put("name", "tiago")
				.put("nickname", "ljtfreitas");

		assertEquals("http://my.api.com/path?age=32&name=tiago&nickname=ljtfreitas", linkURITemplate.expand(parameters).toString());
	}

	@Test
	public void shouldDiscardQueryParameterContinuedVariableWhenParameterIsNotPresent() {
		LinkURITemplate linkURITemplate = new LinkURITemplate("http://my.api.com/path?age=32{&name}");

		assertEquals("http://my.api.com/path?age=32", linkURITemplate.expand().toString());
	}
}
