package com.restify.http.contract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.metadata.EndpointHeader;
import com.restify.http.metadata.EndpointMethod;
import com.restify.http.metadata.EndpointMethodParameter;
import com.restify.http.metadata.EndpointTarget;
import com.restify.http.metadata.EndpointType;
import com.restify.http.metadata.Parameters;
import com.restify.http.metadata.reflection.SimpleGenericArrayType;
import com.restify.http.metadata.reflection.SimpleParameterizedType;
import com.restify.http.metadata.reflection.SimpleWildcardType;

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
		assertEquals(String.class, endpointMethod.get().returnType());

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
		assertEquals(String.class, endpointMethod.get().returnType());

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
		assertEquals(String.class, endpointMethod.get().returnType());
	}

	@Test
	public void shouldMergeEndpointHeadersOnTypeAndMethod() throws Exception {
		EndpointType endpointType = contract.read(endpointTarget);

		assertEquals(MyApiType.class, endpointType.javaType());

		Optional<EndpointMethod> endpointMethod = endpointType.find(MyApiType.class.getMethod("mergeHeaders"));
		assertTrue(endpointMethod.isPresent());

		assertEquals("GET", endpointMethod.get().httpMethod());
		assertEquals("http://my.api.com/mergeHeaders", endpointMethod.get().path());
		assertEquals(String.class, endpointMethod.get().returnType());

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
		assertEquals(Void.TYPE, endpointMethod.get().returnType());

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
		assertEquals(Void.TYPE, endpointMethod.get().returnType());
	}

	@Test
	public void shouldReadMetadataOfMethodWithQueryStringParameter() throws Exception {
		EndpointType endpointType = contract.read(endpointTarget);

		assertEquals(MyApiType.class, endpointType.javaType());

		Optional<EndpointMethod> endpointMethod = endpointType.find(MyApiType.class.getMethod("queryString", new Class[]{Parameters.class}));

		assertTrue(endpointMethod.isPresent());

		assertEquals("GET", endpointMethod.get().httpMethod());
		assertEquals("http://my.api.com/query", endpointMethod.get().path());
		assertEquals(Void.class, endpointMethod.get().returnType());

		Optional<EndpointMethodParameter> queryStringParameter = endpointMethod.get().parameters().get(0);
		assertTrue(queryStringParameter.isPresent());
		assertEquals("parameters", queryStringParameter.get().name());
		assertTrue(queryStringParameter.get().ofQuery());
	}

	@Test
	public void shouldReadMetadataOfInterfaceWithInheritance() throws Exception {
		EndpointType endpointType = contract.read(new EndpointTarget(MyInheritanceApiType.class));

		Optional<EndpointMethod> endpointMethod = endpointType.find(MyInheritanceApiType.class.getMethod("method"));

		assertTrue(endpointMethod.isPresent());
		assertEquals("http://my.api.com/simple", endpointMethod.get().path());

		Optional<EndpointMethod> inheritedEndpointMethod = endpointType.find(MyInheritanceApiType.class.getMethod("inheritedMethod"));
		
		assertTrue(inheritedEndpointMethod.isPresent());
		assertEquals("http://my.api.com/inherited", inheritedEndpointMethod.get().path());
		
	}

	@Test
	public void shouldReadMetadataOfInterfaceWithGenerics() throws Exception {
		EndpointType endpointType = contract.read(new EndpointTarget(MySpecificApi.class));
		
		Optional<EndpointMethod> endpointMethodCreate = endpointType.find(MySpecificApi.class.getMethod("create", new Class[]{Object.class}));
		assertTrue(endpointMethodCreate.isPresent());
		assertEquals("http://my.model.api/create", endpointMethodCreate.get().path());	

		Optional<EndpointMethod> endpointMethodFind = endpointType.find(MySpecificApi.class.getMethod("find", new Class[]{int.class}));
		assertTrue(endpointMethodFind.isPresent());
		assertEquals("http://my.model.api/find", endpointMethodFind.get().path());
		assertEquals(MyModel.class, endpointMethodFind.get().returnType());

		Optional<EndpointMethod> endpointMethodAll = endpointType.find(MySpecificApi.class.getMethod("all"));
		assertTrue(endpointMethodAll.isPresent());
		assertEquals("http://my.model.api/all", endpointMethodAll.get().path());
		assertEquals(new SimpleParameterizedType(Collection.class, null, MyModel.class), endpointMethodAll.get().returnType());

		Optional<EndpointMethod> endpointMethodAllAsList = endpointType.find(MySpecificApi.class.getMethod("allAsList"));
		assertTrue(endpointMethodAllAsList.isPresent());
		assertEquals("http://my.model.api/all", endpointMethodAllAsList.get().path());
		assertEquals(new SimpleParameterizedType(List.class, null, MyModel.class), endpointMethodAllAsList.get().returnType());

		Optional<EndpointMethod> endpointMethodAllAsArray = endpointType.find(MySpecificApi.class.getMethod("allAsArray"));
		assertTrue(endpointMethodAllAsArray.isPresent());
		assertEquals("http://my.model.api/all", endpointMethodAllAsArray.get().path());
		assertEquals(new SimpleGenericArrayType(MyModel.class), endpointMethodAllAsArray.get().returnType());

		Optional<EndpointMethod> endpointMethodMyModelArray = endpointType.find(MySpecificApi.class.getMethod("myModelArray"));
		assertTrue(endpointMethodMyModelArray.isPresent());
		assertEquals("http://my.model.api/all", endpointMethodMyModelArray.get().path());
		assertEquals(MyModel[].class, endpointMethodMyModelArray.get().returnType());

		Optional<EndpointMethod> endpointMethodMyModelAsMap = endpointType.find(MySpecificApi.class.getMethod("myModelAsMap"));
		assertTrue(endpointMethodMyModelAsMap.isPresent());
		assertEquals("http://my.model.api/all", endpointMethodMyModelAsMap.get().path());
		assertEquals(new SimpleParameterizedType(Map.class, null, String.class, MyModel.class), endpointMethodMyModelAsMap.get().returnType());

		Optional<EndpointMethod> endpointMethodAllAsMap = endpointType.find(MySpecificApi.class.getMethod("allAsMap"));
		assertTrue(endpointMethodAllAsMap.isPresent());
		assertEquals("http://my.model.api/all", endpointMethodAllAsMap.get().path());
		assertEquals(new SimpleParameterizedType(Map.class, null, String.class, MyModel.class), endpointMethodAllAsMap.get().returnType());

		Optional<EndpointMethod> endpointMethodAnyType = endpointType.find(MySpecificApi.class.getMethod("anyAsMap"));
		assertTrue(endpointMethodAnyType.isPresent());
		assertEquals("http://my.model.api/any", endpointMethodAnyType.get().path());
		assertEquals(new SimpleParameterizedType(Map.class, null, new SimpleWildcardType(new Type[]{Number.class}, new Type[0]), new SimpleWildcardType(new Type[]{MyModel.class}, new Type[0])),
				endpointMethodAnyType.get().returnType());
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
	}

	@Path("http://my.api.com")
	interface MyBaseApiType {
		
		@Path("/inherited") @Post
		public String inheritedMethod();
	}

	interface MyInheritanceApiType extends MyBaseApiType {

		@Path("/simple") @Get
		public String method();
	}

	interface MyGenericApiType<T> {
		
		@Path("/create") @Post
		public void create(T type);

		@Path("/find") @Get
		public T find(int id);

		@Path("/all") @Get
		public Collection<T> all();

		@Path("/all") @Get
		public List<T> allAsList();

		@Path("/all") @Get
		public T[] allAsArray();
		
		@Path("/all") @Get
		public Map<String, T> allAsMap();
		
		@Path("/any") @Get
		public Map<? extends Number, ? extends T> anyAsMap();
	}
	
	@Path("http://my.model.api")
	interface MySpecificApi extends MyGenericApiType<MyModel> {
		
		@Path("/update") @Put
		public MyModel update(MyModel myModel);

		@Path("/all") @Get
		public MyModel[] myModelArray();

		@Path("/all") @Get
		public Map<String, MyModel> myModelAsMap();
	}
	
	class MyModel {
	}
}
