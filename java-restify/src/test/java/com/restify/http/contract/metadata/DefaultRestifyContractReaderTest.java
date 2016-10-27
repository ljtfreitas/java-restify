package com.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import com.restify.http.contract.Put;
import com.restify.http.contract.QueryParameters;
import com.restify.http.contract.metadata.reflection.SimpleGenericArrayType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;
import com.restify.http.contract.metadata.reflection.SimpleWildcardType;

public class DefaultRestifyContractReaderTest {

	private EndpointTarget myApiTypeTarget;

	private EndpointTarget myInheritanceApiTarget;

	private EndpointTarget myGenericSpecificApiTarget;

	private EndpointTarget myContextApiTarget;

	private DefaultRestifyContractReader restifyContractReader;

	@Before
	public void setup() {
		myApiTypeTarget = new EndpointTarget(MyApiType.class);

		myInheritanceApiTarget = new EndpointTarget(MyInheritanceApiType.class);

		myGenericSpecificApiTarget = new EndpointTarget(MySpecificApi.class);

		myContextApiTarget = new EndpointTarget(MyContextApi.class, "http://my.api.com");

		restifyContractReader = new DefaultRestifyContractReader();
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasSingleParameter() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("method", new Class[] { String.class }));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/{path}", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());

		Optional<EndpointMethodParameter> parameter = endpointMethod.parameters().find("path");
		assertTrue(parameter.isPresent());

		assertEquals(0, parameter.get().position());
		assertTrue(parameter.get().ofPath());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasMultiplesParameters() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("method", new Class[] { String.class, String.class, Object.class }));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/{path}", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());

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
		EndpointMethod endpointMethod = restifyContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("pathWithoutSlash"));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/path", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodMergingEndpointHeadersDeclaredOnTypeWithDeclaredOnMethod() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("mergeHeaders"));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/mergeHeaders", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());

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
		EndpointMethod endpointMethod = restifyContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("customizedNames", new Class[] { String.class, String.class }));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/{customArgumentPath}", endpointMethod.path());
		assertEquals(Void.TYPE, endpointMethod.returnType().classType());

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
	public void shouldCreateEndpointMethodOfMethodWithHttpMethodMetaAnnotation() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("metaAnnotationOfHttpMethod"));

		assertEquals("POST", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/some-method", endpointMethod.path());
		assertEquals(Void.TYPE, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodOfMethodWithQueryStringParameter() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("queryString", new Class[] { Parameters.class }));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/query", endpointMethod.path());
		assertEquals(Void.class, endpointMethod.returnType().classType());

		Optional<EndpointMethodParameter> queryStringParameter = endpointMethod.parameters().get(0);
		assertTrue(queryStringParameter.isPresent());
		assertEquals("parameters", queryStringParameter.get().name());
		assertTrue(queryStringParameter.get().ofQuery());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenMethodHasMoreThanOneBodyParameter() throws Exception {
		new DefaultRestifyContractReader().read(myApiTypeTarget,
				MyApiType.class.getMethod("methodWithTwoBodyParameters", new Class[] { Object.class, Object.class }));
	}

	@Test
	public void shouldCreateEndpointMethodWhenInterfaceHasAInheritance() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myInheritanceApiTarget,
				MyInheritanceApiType.class.getMethod("method"));

		assertEquals("http://my.api.com/simple", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodIsInherited() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myInheritanceApiTarget,
				MyInheritanceApiType.class.getMethod("inheritedMethod"));

		assertEquals("http://my.api.com/inherited", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasAGenericParameter() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("create", new Class[] { Object.class }));

		assertEquals("http://my.model.api/create", endpointMethod.path());

		Optional<EndpointMethodParameter> parameter = endpointMethod.parameters().get(0);
		assertTrue(parameter.isPresent());
		assertEquals("type", parameter.get().name());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsASimpleGenericType() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("find", new Class[] { int.class }));

		assertEquals("http://my.model.api/find", endpointMethod.path());
		assertEquals(MyModel.class, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsACollectionWithGenericType() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("allAsList"));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(List.class, null, MyModel.class), endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsGenericArray() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("allAsArray"));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleGenericArrayType(MyModel.class), endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsArray() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("myModelArray"));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(MyModel[].class, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMap() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("myModelAsMap"));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(Map.class, null, String.class, MyModel.class),
				endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMapWithGenericValue() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("allAsMap"));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(Map.class, null, String.class, MyModel.class),
				endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMapWithGenericKeyAndValue() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("anyAsMap"));

		assertEquals("http://my.model.api/any", endpointMethod.path());
		assertEquals(
				new SimpleParameterizedType(Map.class, null,
						new SimpleWildcardType(new Type[] { Number.class }, new Type[0]),
						new SimpleWildcardType(new Type[] { MyModel.class }, new Type[0])),
				endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenTargetHasEndpointUrl() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myContextApiTarget,
				MyContextApi.class.getMethod("method"));

		assertEquals("http://my.api.com/context/any", endpointMethod.path());
	}

	@Path("http://my.api.com")
	@Header(name = "X-My-Type", value = "MyApiType")
	interface MyApiType {

		@Path("/{path}")
		@Method("GET")
		public String method(@PathParameter String path);

		@Path("/{path}")
		@Method("GET")
		@Header(name = "Content-Type", value = "{contentType}")
		public String method(String path, @HeaderParameter String contentType, @BodyParameter Object body);

		@Path("path")
		@Method("GET")
		public String pathWithoutSlash();

		@Path("/mergeHeaders")
		@Method("GET")
		@Header(name = "Content-Type", value = "application/json")
		@Header(name = "User-Agent", value = "Restify-Agent")
		public String mergeHeaders();

		@Path("/{customArgumentPath}")
		@Method("GET")
		@Header(name = "Content-Type", value = "{customArgumentContentType}")
		public void customizedNames(@PathParameter("customArgumentPath") String path,
				@HeaderParameter("customArgumentContentType") String contentType);

		@Path("/some-method")
		@Post
		public void metaAnnotationOfHttpMethod();

		@Path("/query")
		@Get
		public Void queryString(@QueryParameters Parameters parameters);

		@Path("/twoBodyParameters")
		@Get
		public String methodWithTwoBodyParameters(@BodyParameter Object first, @BodyParameter Object second);
	}

	@Path("http://my.api.com")
	interface MyBaseApiType {

		@Path("/inherited")
		@Post
		public String inheritedMethod();
	}

	interface MyInheritanceApiType extends MyBaseApiType {

		@Path("/simple")
		@Get
		public String method();
	}

	interface MyGenericApiType<T> {

		@Path("/create")
		@Post
		public void create(T type);

		@Path("/find")
		@Get
		public T find(int id);

		@Path("/all")
		@Get
		public Collection<T> all();

		@Path("/all")
		@Get
		public List<T> allAsList();

		@Path("/all")
		@Get
		public T[] allAsArray();

		@Path("/all")
		@Get
		public Map<String, T> allAsMap();

		@Path("/any")
		@Get
		public Map<? extends Number, ? extends T> anyAsMap();
	}

	@Path("http://my.model.api")
	interface MySpecificApi extends MyGenericApiType<MyModel> {

		@Path("/update")
		@Put
		public MyModel update(MyModel myModel);

		@Path("/all")
		@Get
		public MyModel[] myModelArray();

		@Path("/all")
		@Get
		public Map<String, MyModel> myModelAsMap();
	}

	@Path("/context")
	interface MyContextApi {

		@Path("/any")
		@Method("GET")
		public String method();
	}

	class MyModel {
	}
}
