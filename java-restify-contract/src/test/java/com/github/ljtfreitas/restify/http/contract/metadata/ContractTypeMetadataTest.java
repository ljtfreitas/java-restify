package com.github.ljtfreitas.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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

		assertEquals(2, contractTypeMetadata.headers().size());

		List<Header> headers = new ArrayList<>(contractTypeMetadata.headers());

		assertEquals("X-My-Header-1", headers.get(0).name());
		assertEquals("MyHeader1", headers.get(0).value());

		assertEquals("X-My-Header-2", headers.get(1).name());
		assertEquals("MyHeader2", headers.get(1).value());
	}

	@Test
	public void shouldReadMetadataOfTypeWithArrayOfHeaders() {
		ContractTypeMetadata contractTypeMetadata = new ContractTypeMetadata(MyApiTypeWithArrayOfHeaders.class);

		assertSame(MyApiTypeWithArrayOfHeaders.class, contractTypeMetadata.javaType());

		assertTrue(contractTypeMetadata.path().isPresent());
		assertEquals("http://my.api.com", contractTypeMetadata.path().get().value());

		assertEquals(1, contractTypeMetadata.paths().length);
		assertEquals(2, contractTypeMetadata.headers().size());

		List<Header> headers = new ArrayList<>(contractTypeMetadata.headers());
		
		assertEquals("X-My-Header-1", headers.get(0).name());
		assertEquals("MyHeader1", headers.get(0).value());

		assertEquals("X-My-Header-2", headers.get(1).name());
		assertEquals("MyHeader2", headers.get(1).value());
	}

	@Test
	public void shouldReadMetadataOfTypeThatInheritsOther() {
		ContractTypeMetadata contractTypeMetadata = new ContractTypeMetadata(MySpecificApi.class);

		assertTrue(contractTypeMetadata.path().isPresent());
		assertEquals("/specific", contractTypeMetadata.path().get().value());

		assertEquals(2, contractTypeMetadata.paths().length);
		assertEquals("http://my.api.com", contractTypeMetadata.paths()[0].value());
		assertEquals("/specific", contractTypeMetadata.paths()[1].value());

		List<Header> headers = new ArrayList<>(contractTypeMetadata.headers());

		assertEquals(3, contractTypeMetadata.headers().size());

		assertEquals("X-Basic-Header", headers.get(0).name());
		assertEquals("Any", headers.get(0).value());

		assertEquals("X-My-Generic-Header", headers.get(1).name());
		assertEquals("MyGenericHeader", headers.get(1).value());

		assertEquals("X-My-Specific-Header", headers.get(2).name());
		assertEquals("MySpecificHeader", headers.get(2).value());
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
