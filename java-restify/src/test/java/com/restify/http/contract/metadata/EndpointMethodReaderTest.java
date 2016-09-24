package com.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.contract.BodyParameter;
import com.restify.http.contract.Get;
import com.restify.http.contract.Header;
import com.restify.http.contract.HeaderParameter;
import com.restify.http.contract.Method;
import com.restify.http.contract.Parameters;
import com.restify.http.contract.Path;
import com.restify.http.contract.PathParameter;
import com.restify.http.contract.Post;
import com.restify.http.contract.QueryParameters;
import com.restify.http.contract.metadata.EndpointHeader;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointMethodReader;
import com.restify.http.contract.metadata.EndpointTarget;

public class EndpointMethodReaderTest {

	private EndpointTarget endpointTarget;

	@Before
	public void setup() {
		endpointTarget = new EndpointTarget(MyApiType.class);
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasSingleParameter() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethodReader(endpointTarget)
				.read(MyApiType.class.getMethod("method", new Class[]{String.class}));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/{path}", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType());

		Optional<EndpointMethodParameter> parameter = endpointMethod.parameters().find("path");
		assertTrue(parameter.isPresent());

		assertEquals(0, parameter.get().position());
		assertTrue(parameter.get().ofPath());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasMultiplesParameters() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethodReader(endpointTarget)
				.read(MyApiType.class.getMethod("method", new Class[]{String.class, String.class, Object.class}));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/{path}", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType());

		Optional<EndpointMethodParameter> pathParameter = endpointMethod.parameters().get(0);
		assertTrue(pathParameter.isPresent());
		assertEquals("path", pathParameter.get().name());
		assertTrue(pathParameter.get().ofPath());

		Optional<EndpointMethodParameter> headerParameter = endpointMethod.parameters().get(1);
		assertTrue(headerParameter.isPresent());
		assertEquals("contentType", headerParameter.get().name());
		assertTrue(headerParameter.get().ofHeader());

		Optional<EndpointMethodParameter> bodyParameter = endpointMethod.parameters().get(2);
		assertTrue(bodyParameter.isPresent());
		assertEquals("body", bodyParameter.get().name());
		assertTrue(bodyParameter.get().ofBody());
	}

	@Test
	public void shouldCreateEndpointMethodWhenPathAnnotationOnMethodHasNoSlashOnStart() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethodReader(endpointTarget)
				.read(MyApiType.class.getMethod("pathWithoutSlash"));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/path", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType());
	}

	@Test
	public void shouldCreateEndpointMethodMergingEndpointHeadersDeclaredOnTypeWithDeclaredOnMethod() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethodReader(endpointTarget)
				.read(MyApiType.class.getMethod("mergeHeaders"));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/mergeHeaders", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType());

		Optional<EndpointHeader> myTypeHeader = endpointMethod.headers().first("X-My-Type");
		assertTrue(myTypeHeader.isPresent());
		assertEquals("MyApiType", myTypeHeader.get().value());

		Optional<EndpointHeader> contentTypeHeader = endpointMethod.headers().first("Content-Type");
		assertTrue(contentTypeHeader.isPresent());
		assertEquals("application/json", contentTypeHeader.get().value());

		Optional<EndpointHeader> userAgentHeader = endpointMethod.headers().first("User-Agent");
		assertTrue(userAgentHeader.isPresent());
		assertEquals("Restify-Agent", userAgentHeader.get().value());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasCustomizedParameterNames() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethodReader(endpointTarget)
				.read(MyApiType.class.getMethod("customizedNames", new Class[]{String.class, String.class}));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/{customArgumentPath}", endpointMethod.path());
		assertEquals(Void.TYPE, endpointMethod.returnType());

		Optional<EndpointMethodParameter> pathParameter = endpointMethod.parameters().get(0);
		assertTrue(pathParameter.isPresent());
		assertEquals("customArgumentPath", pathParameter.get().name());
		assertTrue(pathParameter.get().ofPath());

		Optional<EndpointMethodParameter> headerParameter = endpointMethod.parameters().get(1);
		assertTrue(headerParameter.isPresent());
		assertEquals("customArgumentContentType", headerParameter.get().name());
		assertTrue(headerParameter.get().ofHeader());
	}

	@Test
	public void shouldReadMetadataOfMethodWithHttpMethodMetaAnnotation() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethodReader(endpointTarget)
				.read(MyApiType.class.getMethod("metaAnnotationOfHttpMethod"));

		assertEquals("POST", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/some-method", endpointMethod.path());
		assertEquals(Void.TYPE, endpointMethod.returnType());
	}

	@Test
	public void shouldReadMetadataOfMethodWithQueryStringParameter() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethodReader(endpointTarget)
				.read(MyApiType.class.getMethod("queryString", new Class[]{Parameters.class}));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/query", endpointMethod.path());
		assertEquals(Void.class, endpointMethod.returnType());

		Optional<EndpointMethodParameter> queryStringParameter = endpointMethod.parameters().get(0);
		assertTrue(queryStringParameter.isPresent());
		assertEquals("parameters", queryStringParameter.get().name());
		assertTrue(queryStringParameter.get().ofQuery());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenMethodHasMoreThanOneBodyParameter() throws Exception {
		new EndpointMethodReader(endpointTarget)
				.read(MyApiType.class.getMethod("methodWithTwoBodyParameters", new Class[]{Object.class, Object.class}));
	}

	@Path("http://my.api.com")
	@Header(name = "X-My-Type", value = "MyApiType")
	interface MyApiType {

		@Path("/{path}") @Method("GET")
		public String method(@PathParameter String path);

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

		@Path("/query") @Get
		public Void queryString(@QueryParameters Parameters parameters);

		@Path("/twoBodyParameters") @Get
		public String methodWithTwoBodyParameters(@BodyParameter Object first, @BodyParameter Object second);
	}

}
