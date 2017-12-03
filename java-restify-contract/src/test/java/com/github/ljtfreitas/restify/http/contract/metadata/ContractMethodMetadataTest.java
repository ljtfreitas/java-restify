package com.github.ljtfreitas.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.Headers;
import com.github.ljtfreitas.restify.http.contract.Method;
import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.Post;

public class ContractMethodMetadataTest {

	@Test
	public void shouldReadMetadataOfSimpleMethod() throws Exception {
		java.lang.reflect.Method javaMethod = MyApiType.class.getMethod("method");

		ContractMethodMetadata contractMethodMetadata = new ContractMethodMetadata(javaMethod);

		assertEquals("/path", contractMethodMetadata.path().get().value());
		assertEquals("GET", contractMethodMetadata.httpMethod().value());

		assertEquals(1, contractMethodMetadata.headers().size());
		
		List<Header> headers = new ArrayList<>(contractMethodMetadata.headers());
		
		assertEquals("X-My-Header", headers.get(0).name());
		assertEquals("MyHeader", headers.get(0).value());
	}

	@Test
	public void shouldReadMetadataOfMethodWithMetaHttpMethodAnnotation() throws Exception {
		java.lang.reflect.Method javaMethod = MyApiType.class.getMethod("methodWithMetaHttpMethodAnnotation");

		ContractMethodMetadata contractMethodMetadata = new ContractMethodMetadata(javaMethod);

		assertEquals("/path", contractMethodMetadata.path().get().value());
		assertEquals("POST", contractMethodMetadata.httpMethod().value());

		assertTrue(contractMethodMetadata.headers().isEmpty());
	}

	@Test
	public void shouldReadMetadataOfMethodWithArrayOfHeaders() throws Exception {
		java.lang.reflect.Method javaMethod = MyApiType.class.getMethod("methodWithArrayOfHeaders");

		ContractMethodMetadata contractMethodMetadata = new ContractMethodMetadata(javaMethod);

		assertEquals("/path", contractMethodMetadata.path().get().value());
		assertEquals("GET", contractMethodMetadata.httpMethod().value());

		assertEquals(2, contractMethodMetadata.headers().size());

		List<Header> headers = new ArrayList<>(contractMethodMetadata.headers());

		assertEquals("X-My-Header-1", headers.get(0).name());
		assertEquals("MyHeader1", headers.get(0).value());

		assertEquals("X-My-Header-2", headers.get(1).name());
		assertEquals("MyHeader2", headers.get(1).value());
	}

	@Test
	public void shouldReturnEmptyPathWhenMethodHasNoPathAnnotation() throws Exception {
		java.lang.reflect.Method javaMethod = MyApiType.class.getMethod("methodWithoutPathAnnotation");

		ContractMethodMetadata contractMethodMetadata = new ContractMethodMetadata(javaMethod);

		assertFalse(contractMethodMetadata.path().isPresent());
		assertEquals("GET", contractMethodMetadata.httpMethod().value());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenMethodHasNoHttpMethodAnnoation() throws Exception {
		java.lang.reflect.Method javaMethod = MyApiType.class.getMethod("methodWithoutHttpMethodAnnotation");

		new ContractMethodMetadata(javaMethod);
	}

	interface MyApiType {

		@Path("/path")
		@Method("GET")
		@Header(name = "X-My-Header", value = "MyHeader")
		public String method();

		@Path("/path")
		@Post
		public String methodWithMetaHttpMethodAnnotation();

		@Path("/path")
		@Method("GET")
		@Headers({
			@Header(name = "X-My-Header-1", value = "MyHeader1"),
			@Header(name = "X-My-Header-2", value = "MyHeader2")})
		public String methodWithArrayOfHeaders();

		@Get
		public String methodWithoutPathAnnotation();

		@Path("/path")
		public String methodWithoutHttpMethodAnnotation();
	}
}
