package com.restify.http.contract.metadata.reflection;

import static org.junit.Assert.*;

import org.junit.Test;

import com.restify.http.contract.Header;
import com.restify.http.contract.Headers;
import com.restify.http.contract.Path;
import com.restify.http.contract.metadata.reflection.JavaTypeMetadata;

public class JavaTypeMetadataTest {

	@Test
	public void shouldReadMetadataOfType() {
		JavaTypeMetadata javaTypeMetadata = new JavaTypeMetadata(MyApiType.class);

		assertSame(MyApiType.class, javaTypeMetadata.javaType());

		assertTrue(javaTypeMetadata.path().isPresent());
		assertEquals("http://my.api.com", javaTypeMetadata.path().get().value());

		assertEquals(1, javaTypeMetadata.paths().length);

		assertEquals(2, javaTypeMetadata.headers().length);

		assertEquals("X-My-Header-1", javaTypeMetadata.headers()[0].name());
		assertEquals("MyHeader1", javaTypeMetadata.headers()[0].value());

		assertEquals("X-My-Header-2", javaTypeMetadata.headers()[1].name());
		assertEquals("MyHeader2", javaTypeMetadata.headers()[1].value());
	}

	@Test
	public void shouldReadMetadataOfTypeWithArrayOfHeaders() {
		JavaTypeMetadata javaTypeMetadata = new JavaTypeMetadata(MyApiTypeWithArrayOfHeaders.class);

		assertSame(MyApiTypeWithArrayOfHeaders.class, javaTypeMetadata.javaType());

		assertTrue(javaTypeMetadata.path().isPresent());
		assertEquals("http://my.api.com", javaTypeMetadata.path().get().value());

		assertEquals(1, javaTypeMetadata.paths().length);

		assertEquals(2, javaTypeMetadata.headers().length);

		assertEquals("X-My-Header-1", javaTypeMetadata.headers()[0].name());
		assertEquals("MyHeader1", javaTypeMetadata.headers()[0].value());

		assertEquals("X-My-Header-2", javaTypeMetadata.headers()[1].name());
		assertEquals("MyHeader2", javaTypeMetadata.headers()[1].value());
	}

	@Test
	public void shouldReadMetadataOfTypeThatInheritsOther() {
		JavaTypeMetadata javaTypeMetadata = new JavaTypeMetadata(MySpecificApi.class);

		assertTrue(javaTypeMetadata.path().isPresent());
		assertEquals("/specific", javaTypeMetadata.path().get().value());

		assertEquals(2, javaTypeMetadata.paths().length);
		assertEquals("http://my.api.com", javaTypeMetadata.paths()[0].value());
		assertEquals("/specific", javaTypeMetadata.paths()[1].value());

		assertEquals(3, javaTypeMetadata.headers().length);

		assertEquals("X-Basic-Header", javaTypeMetadata.headers()[0].name());
		assertEquals("Any", javaTypeMetadata.headers()[0].value());

		assertEquals("X-My-Generic-Header", javaTypeMetadata.headers()[1].name());
		assertEquals("MyGenericHeader", javaTypeMetadata.headers()[1].value());

		assertEquals("X-My-Specific-Header", javaTypeMetadata.headers()[2].name());
		assertEquals("MySpecificHeader", javaTypeMetadata.headers()[2].value());

	}

	@Path("http://my.api.com")
	@Header(name = "X-My-Header-1", value = "MyHeader1")
	@Header(name = "X-My-Header-2", value = "MyHeader2")
	interface MyApiType {
	}

	@Path("http://my.api.com")
	@Headers({ @Header(name = "X-My-Header-1", value = "MyHeader1"),
			@Header(name = "X-My-Header-2", value = "MyHeader2") })
	interface MyApiTypeWithArrayOfHeaders {
	}

	@Header(name = "X-Basic-Header", value = "Any")
	interface BasicHeaders {
	}

	@Path("http://my.api.com")
	@Header(name = "X-My-Generic-Header", value = "MyGenericHeader")
	interface MyBaseApi extends BasicHeaders {
	}

	@Path("/specific")
	@Header(name = "X-My-Specific-Header", value = "MySpecificHeader")
	interface MySpecificApi extends MyBaseApi {
	}
}
