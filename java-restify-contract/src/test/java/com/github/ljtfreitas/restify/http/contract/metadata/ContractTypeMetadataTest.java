package com.github.ljtfreitas.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.Headers;
import com.github.ljtfreitas.restify.http.contract.Path;

public class ContractTypeMetadataTest {

	@Test
	public void shouldReadMetadataOfType() {
		ContractTypeMetadata contractTypeMetadata = new ContractTypeMetadata(MyApiType.class);

		assertSame(MyApiType.class, contractTypeMetadata.javaType());

		assertTrue(contractTypeMetadata.path().isPresent());
		assertEquals("http://my.api.com", contractTypeMetadata.path().get().value());

		assertEquals(1, contractTypeMetadata.paths().length);

		assertEquals(2, contractTypeMetadata.headers().length);

		assertEquals("X-My-Header-1", contractTypeMetadata.headers()[0].name());
		assertEquals("MyHeader1", contractTypeMetadata.headers()[0].value());

		assertEquals("X-My-Header-2", contractTypeMetadata.headers()[1].name());
		assertEquals("MyHeader2", contractTypeMetadata.headers()[1].value());
	}

	@Test
	public void shouldReadMetadataOfTypeWithArrayOfHeaders() {
		ContractTypeMetadata contractTypeMetadata = new ContractTypeMetadata(MyApiTypeWithArrayOfHeaders.class);

		assertSame(MyApiTypeWithArrayOfHeaders.class, contractTypeMetadata.javaType());

		assertTrue(contractTypeMetadata.path().isPresent());
		assertEquals("http://my.api.com", contractTypeMetadata.path().get().value());

		assertEquals(1, contractTypeMetadata.paths().length);

		assertEquals(2, contractTypeMetadata.headers().length);

		assertEquals("X-My-Header-1", contractTypeMetadata.headers()[0].name());
		assertEquals("MyHeader1", contractTypeMetadata.headers()[0].value());

		assertEquals("X-My-Header-2", contractTypeMetadata.headers()[1].name());
		assertEquals("MyHeader2", contractTypeMetadata.headers()[1].value());
	}

	@Test
	public void shouldReadMetadataOfTypeThatInheritsOther() {
		ContractTypeMetadata contractTypeMetadata = new ContractTypeMetadata(MySpecificApi.class);

		assertTrue(contractTypeMetadata.path().isPresent());
		assertEquals("/specific", contractTypeMetadata.path().get().value());

		assertEquals(2, contractTypeMetadata.paths().length);
		assertEquals("http://my.api.com", contractTypeMetadata.paths()[0].value());
		assertEquals("/specific", contractTypeMetadata.paths()[1].value());

		assertEquals(3, contractTypeMetadata.headers().length);

		assertEquals("X-Basic-Header", contractTypeMetadata.headers()[0].name());
		assertEquals("Any", contractTypeMetadata.headers()[0].value());

		assertEquals("X-My-Generic-Header", contractTypeMetadata.headers()[1].name());
		assertEquals("MyGenericHeader", contractTypeMetadata.headers()[1].value());

		assertEquals("X-My-Specific-Header", contractTypeMetadata.headers()[2].name());
		assertEquals("MySpecificHeader", contractTypeMetadata.headers()[2].value());

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
