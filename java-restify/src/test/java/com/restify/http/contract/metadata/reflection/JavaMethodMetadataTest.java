package com.restify.http.contract.metadata.reflection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.restify.http.contract.Header;
import com.restify.http.contract.Headers;
import com.restify.http.contract.Method;
import com.restify.http.contract.Path;
import com.restify.http.contract.Post;
import com.restify.http.contract.metadata.reflection.JavaMethodMetadata;

public class JavaMethodMetadataTest {

	@Test
	public void shouldReadMetadataOfSimpleMethod() throws Exception {
		java.lang.reflect.Method javaMethod = MyApiType.class.getMethod("method");

		JavaMethodMetadata javaMethodMetadata = new JavaMethodMetadata(javaMethod);

		assertEquals("/path", javaMethodMetadata.path().value());
		assertEquals("GET", javaMethodMetadata.httpMethod().value());

		assertEquals(1, javaMethodMetadata.headers().length);
		assertEquals("X-My-Header", javaMethodMetadata.headers()[0].name());
		assertEquals("MyHeader", javaMethodMetadata.headers()[0].value());
	}

	@Test
	public void shouldReadMetadataOfMethodWithMetaHttpMethodAnnotation() throws Exception {
		java.lang.reflect.Method javaMethod = MyApiType.class.getMethod("methodWithMetaHttpMethodAnnotation");

		JavaMethodMetadata javaMethodMetadata = new JavaMethodMetadata(javaMethod);

		assertEquals("/path", javaMethodMetadata.path().value());
		assertEquals("POST", javaMethodMetadata.httpMethod().value());

		assertEquals(0, javaMethodMetadata.headers().length);
	}

	@Test
	public void shouldReadMetadataOfMethodWithArrayOfHeaders() throws Exception {
		java.lang.reflect.Method javaMethod = MyApiType.class.getMethod("methodWithArrayOfHeaders");

		JavaMethodMetadata javaMethodMetadata = new JavaMethodMetadata(javaMethod);

		assertEquals("/path", javaMethodMetadata.path().value());
		assertEquals("GET", javaMethodMetadata.httpMethod().value());

		assertEquals(2, javaMethodMetadata.headers().length);

		assertEquals("X-My-Header-1", javaMethodMetadata.headers()[0].name());
		assertEquals("MyHeader1", javaMethodMetadata.headers()[0].value());

		assertEquals("X-My-Header-2", javaMethodMetadata.headers()[1].name());
		assertEquals("MyHeader2", javaMethodMetadata.headers()[1].value());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenMethodHasNoPathAnnotation() throws Exception {
		java.lang.reflect.Method javaMethod = MyApiType.class.getMethod("methodWithoutPathAnnotation");

		new JavaMethodMetadata(javaMethod);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenMethodHasNoHttpMethodAnnoation() throws Exception {
		java.lang.reflect.Method javaMethod = MyApiType.class.getMethod("methodWithoutHttpMethodAnnotation");

		new JavaMethodMetadata(javaMethod);
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

		public String methodWithoutPathAnnotation();

		@Path("/path")
		public String methodWithoutHttpMethodAnnotation();
	}
}
