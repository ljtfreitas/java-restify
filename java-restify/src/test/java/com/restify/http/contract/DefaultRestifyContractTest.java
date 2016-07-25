package com.restify.http.contract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.metadata.EndpointHeader;
import com.restify.http.metadata.EndpointMethod;
import com.restify.http.metadata.EndpointMethodParameter;
import com.restify.http.metadata.EndpointTarget;
import com.restify.http.metadata.EndpointType;

public class DefaultRestifyContractTest {

	private DefaultRestifyContract contract = new DefaultRestifyContract();

	private EndpointTarget endpointTarget;

	@Before
	public void setup() {
		endpointTarget = new EndpointTarget(MyApiType.class);
	}

	@Test
	public void shouldReadMetadataOfMethodWithSingleParameter() throws Exception {
		EndpointType endpointType = contract.read(endpointTarget);

		assertEquals(MyApiType.class, endpointType.javaType());

		Optional<EndpointMethod> endpointMethod = endpointType.find(MyApiType.class.getMethod("method", new Class[]{String.class}));
		assertTrue(endpointMethod.isPresent());

		assertEquals("GET", endpointMethod.get().httpMethod());
		assertEquals("http://my.api.com/{path}", endpointMethod.get().path());
		assertEquals(String.class, endpointMethod.get().expectedType());

		Optional<EndpointMethodParameter> parameter = endpointMethod.get().parameters().find("path");
		assertTrue(parameter.isPresent());

		assertEquals(0, parameter.get().position());
		assertTrue(parameter.get().ofPath());
	}

	@Test
	public void shouldReadMetadataOfMethodWithMultiplesParameters() throws Exception {
		EndpointType endpointType = contract.read(endpointTarget);

		assertEquals(MyApiType.class, endpointType.javaType());

		Optional<EndpointMethod> endpointMethod = endpointType.find(MyApiType.class.getMethod("method",
				new Class[]{String.class, String.class, Object.class}));

		assertTrue(endpointMethod.isPresent());

		assertEquals("GET", endpointMethod.get().httpMethod());
		assertEquals("http://my.api.com/{path}", endpointMethod.get().path());
		assertEquals(String.class, endpointMethod.get().expectedType());

		Optional<EndpointMethodParameter> pathParameter = endpointMethod.get().parameters().get(0);
		assertTrue(pathParameter.isPresent());
		assertEquals("path", pathParameter.get().name());
		assertTrue(pathParameter.get().ofPath());

		Optional<EndpointMethodParameter> headerParameter = endpointMethod.get().parameters().get(1);
		assertTrue(headerParameter.isPresent());
		assertEquals("contentType", headerParameter.get().name());
		assertTrue(headerParameter.get().ofHeader());

		Optional<EndpointMethodParameter> bodyParameter = endpointMethod.get().parameters().get(2);
		assertTrue(bodyParameter.isPresent());
		assertEquals("body", bodyParameter.get().name());
		assertTrue(bodyParameter.get().ofBody());
	}

	@Test
	public void shouldReadMetadataOfMethodWithPathWithoutSlashOnStart() throws Exception {
		EndpointType endpointType = contract.read(endpointTarget);

		assertEquals(MyApiType.class, endpointType.javaType());

		Optional<EndpointMethod> endpointMethod = endpointType.find(MyApiType.class.getMethod("pathWithoutSlash"));

		assertTrue(endpointMethod.isPresent());

		assertEquals("GET", endpointMethod.get().httpMethod());
		assertEquals("http://my.api.com/path", endpointMethod.get().path());
		assertEquals(String.class, endpointMethod.get().expectedType());
	}

	@Test
	public void shouldMergeEndpointHeadersOnTypeAndMethod() throws Exception {
		EndpointType endpointType = contract.read(endpointTarget);

		assertEquals(MyApiType.class, endpointType.javaType());

		Optional<EndpointMethod> endpointMethod = endpointType.find(MyApiType.class.getMethod("mergeHeaders"));
		assertTrue(endpointMethod.isPresent());

		assertEquals("GET", endpointMethod.get().httpMethod());
		assertEquals("http://my.api.com/mergeHeaders", endpointMethod.get().path());
		assertEquals(String.class, endpointMethod.get().expectedType());

		Optional<EndpointHeader> myTypeHeader = endpointMethod.get().headers().first("X-My-Type");
		assertTrue(myTypeHeader.isPresent());
		assertEquals("MyApiType", myTypeHeader.get().value());

		Optional<EndpointHeader> contentTypeHeader = endpointMethod.get().headers().first("Content-Type");
		assertTrue(contentTypeHeader.isPresent());
		assertEquals("application/json", contentTypeHeader.get().value());

		Optional<EndpointHeader> userAgentHeader = endpointMethod.get().headers().first("User-Agent");
		assertTrue(userAgentHeader.isPresent());
		assertEquals("Restify-Agent", userAgentHeader.get().value());
	}

	@Test
	public void shouldReadMetadataOfMethodWithCustomizedParameterNames() throws Exception {
		EndpointType endpointType = contract.read(endpointTarget);

		assertEquals(MyApiType.class, endpointType.javaType());

		Optional<EndpointMethod> endpointMethod = endpointType.find(MyApiType.class.getMethod("customizedNames",
				new Class[]{String.class, String.class}));

		assertTrue(endpointMethod.isPresent());

		assertEquals("GET", endpointMethod.get().httpMethod());
		assertEquals("http://my.api.com/{customArgumentPath}", endpointMethod.get().path());
		assertEquals(Void.TYPE, endpointMethod.get().expectedType());

		Optional<EndpointMethodParameter> pathParameter = endpointMethod.get().parameters().get(0);
		assertTrue(pathParameter.isPresent());
		assertEquals("customArgumentPath", pathParameter.get().name());
		assertTrue(pathParameter.get().ofPath());

		Optional<EndpointMethodParameter> headerParameter = endpointMethod.get().parameters().get(1);
		assertTrue(headerParameter.isPresent());
		assertEquals("customArgumentContentType", headerParameter.get().name());
		assertTrue(headerParameter.get().ofHeader());
	}

	@Test
	public void shouldReadMetadataOfMethodWithHttpMethodMetaAnnotation() throws Exception {
		EndpointType endpointType = contract.read(endpointTarget);

		assertEquals(MyApiType.class, endpointType.javaType());

		Optional<EndpointMethod> endpointMethod = endpointType.find(MyApiType.class.getMethod("metaAnnotationOfHttpMethod"));

		assertTrue(endpointMethod.isPresent());

		assertEquals("POST", endpointMethod.get().httpMethod());
		assertEquals("http://my.api.com/some-method", endpointMethod.get().path());
		assertEquals(Void.TYPE, endpointMethod.get().expectedType());
	}

	@Path("http://my.api.com")
	@Header(name = "X-My-Type", value = "MyApiType")
	interface MyApiType {

		@Path("/{path}") @Method("GET")
		public String method(String path);

		@Path("/{path}") @Method("GET")
		@Header(name = "Content-Type", value = "{contentType}")
		public String method(String path, @HeaderParameter String contentType, @BodyParameter Object body);

		@Path("path") @Method("GET")
		public String pathWithoutSlash();

		@Path("/mergeHeaders") @Method("GET")
		@Header(name = "Content-Type", value = "application/json")
		@Header(name = "User-Agent", value = "Restify-Agent")
		public String mergeHeaders();

		@Path("/{customArgumentPath}") @Method("GET")
		@Header(name = "Content-Type", value = "{customArgumentContentType}")
		public void customizedNames(@PathParameter("customArgumentPath") String path, @HeaderParameter("customArgumentContentType") String contentType);

		@Path("/some-method") @Post
		public void metaAnnotationOfHttpMethod();
	}
}
