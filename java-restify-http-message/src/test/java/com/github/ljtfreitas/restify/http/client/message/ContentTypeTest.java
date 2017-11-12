package com.github.ljtfreitas.restify.http.client.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ContentTypeTest {

	@Test
	public void shouldParseContentTypeWithOneParameter() {
		String source = "application/json; charset=UTF-8";

		ContentType contentType = ContentType.of(source);

		assertEquals("application/json", contentType.name());
	
		assertFalse(contentType.parameters().empty());
		assertEquals("UTF-8", contentType.parameter("charset").get());

		assertEquals(source, contentType.toString());
	}

	@Test
	public void shouldParseContentTypeWithMultiplesParameters() {
		String source = "multipart/form-data; charset=UTF-8; boundary=abc1234";

		ContentType contentType = ContentType.of(source);

		assertEquals("multipart/form-data", contentType.name());

		assertFalse(contentType.parameters().empty());

		assertEquals("UTF-8", contentType.parameter("charset").get());
		assertEquals("abc1234", contentType.parameter("boundary").get());

		assertEquals(source, contentType.toString());
	}

	@Test
	public void shouldParseContentTypeWithoutParameters() {
		String source = "application/json";

		ContentType contentType = ContentType.of(source);

		assertEquals(source, contentType.name());

		assertTrue(contentType.parameters().empty());

		assertEquals(source, contentType.toString());
	}

	@Test
	public void shouldCreateNewContentTypeWhenParameterIsAdded() {
		ContentType contentType = ContentType.of("application/json");

		ContentType newContentType = contentType.append("charset", "UTF-8");

		assertTrue(contentType.parameters().empty());

		assertFalse(newContentType.parameters().empty());
		assertEquals("application/json", newContentType.name());
		assertEquals("UTF-8", newContentType.parameter("charset").get());
		assertEquals("application/json; charset=UTF-8", newContentType.toString());
	}

	@Test
	public void shouldBeCompatibleWithWildcardType() {
		ContentType wildcardContentType = ContentType.of("*/*");

		ContentType textPlainContentType = ContentType.of("text/plain");

		assertTrue(wildcardContentType.compatible(textPlainContentType));
	}

	@Test
	public void shouldBeCompatibleWithWildcardSpecificType() {
		ContentType genericTextContentType = ContentType.of("text/*");

		ContentType textPlainContentType = ContentType.of("text/plain");

		assertTrue(genericTextContentType.compatible(textPlainContentType));
	}

	@Test
	public void shouldBeIncompatibleBetweenDifferentContentTypes() {
		ContentType jsonContentType = ContentType.of("application/json");

		ContentType xmlContentType = ContentType.of("application/xml");

		assertFalse(jsonContentType.compatible(xmlContentType));
	}

	@Test
	public void shouldBeCompatibleWhenSuffixTypeAreEquals() {
		ContentType jsonVendorContentVersionOne = ContentType.of("application/vnd.bla+json;version=1");

		ContentType jsonVendorContentVersionTwo = ContentType.of("application/vnd.bla+json;version=2");

		assertTrue(jsonVendorContentVersionOne.compatible(jsonVendorContentVersionTwo));
	}

	@Test
	public void shouldBeIncompatibleWhenSuffixTypeAreDifferent() {
		ContentType xmlVendorContent = ContentType.of("application/vnd.bla+xml");

		ContentType jsonVendorContent = ContentType.of("application/vnd.bla+json");

		assertFalse(xmlVendorContent.compatible(jsonVendorContent));
	}
	
	@Test
	public void shouldBeCompatibleWhenSuffixTypeWildcardAreEquals() {
		ContentType halJsonContentType = ContentType.of("application/hal+json");

		ContentType jsonWildcardContentType = ContentType.of("application/*+json");

		assertTrue(halJsonContentType.compatible(jsonWildcardContentType));
		assertTrue(jsonWildcardContentType.compatible(halJsonContentType));
	}

	@Test
	public void shouldBeCompatibleWithWildcardTypeWhenSuffixTypeIsPresent() {
		ContentType wildcardContentType = ContentType.of("*/*");

		ContentType halJsonContentType = ContentType.of("application/hal+json");

		assertTrue(wildcardContentType.compatible(halJsonContentType));
	}
}
