package com.github.ljtfreitas.restify.http.jaxrs.contract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointHeader;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointTarget;
import com.github.ljtfreitas.restify.http.jaxrs.contract.metadata.JaxRsContractReader;
import com.github.ljtfreitas.restify.reflection.SimpleGenericArrayType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;
import com.github.ljtfreitas.restify.reflection.SimpleWildcardType;

public class JaxRsContractReaderTest {

	private EndpointTarget myApiTypeTarget;

	private EndpointTarget myInheritanceApiTarget;

	private EndpointTarget myGenericSpecificApiTarget;

	private EndpointTarget myContextApiTarget;

	private EndpointTarget mySimpleCrudApiTarget;

	private JaxRsContractReader jaxRsContractReader;

	@Before
	public void setup() {
		myApiTypeTarget = new EndpointTarget(MyApiType.class);

		myInheritanceApiTarget = new EndpointTarget(MyInheritanceApiType.class);

		myGenericSpecificApiTarget = new EndpointTarget(MySpecificApi.class);

		myContextApiTarget = new EndpointTarget(MyContextApi.class, "http://my.api.com");

		mySimpleCrudApiTarget = new EndpointTarget(MySimpleCrudApi.class, "http://my.api.com");

		jaxRsContractReader = new JaxRsContractReader();
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasSingleParameter() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("method", new Class[] { String.class }))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/{path}", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());

		Optional<EndpointMethodParameter> parameter = endpointMethod.parameters().find("path");
		assertTrue(parameter.isPresent());

		assertEquals(0, parameter.get().position());
		assertTrue(parameter.get().path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasMultiplesParameters() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("method", new Class[] { String.class, String.class, Object.class }))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/{path}", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());

		Optional<EndpointMethodParameter> pathParameter = endpointMethod.parameters().get(0);
		assertTrue(pathParameter.isPresent());
		assertEquals("path", pathParameter.get().name());
		assertTrue(pathParameter.get().path());

		Optional<EndpointMethodParameter> headerParameter = endpointMethod.parameters().get(1);
		assertTrue(headerParameter.isPresent());
		assertEquals("Content-Type", headerParameter.get().name());
		assertTrue(headerParameter.get().header());

		Optional<EndpointMethodParameter> bodyParameter = endpointMethod.parameters().get(2);
		assertTrue(bodyParameter.isPresent());
		assertEquals("body", bodyParameter.get().name());
		assertTrue(bodyParameter.get().body());
	}

	@Test
	public void shouldCreateEndpointMethodWithHeadersWhenMethodHasHeaderParameters() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("headers", new Class[] { String.class, String.class }))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/headers", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());

		Optional<EndpointMethodParameter> customHeaderParameter = endpointMethod.parameters().get(0);
		assertTrue(customHeaderParameter.isPresent());
		assertEquals("X-Custom-Header", customHeaderParameter.get().name());
		assertTrue(customHeaderParameter.get().header());

		Optional<EndpointMethodParameter> otherCustomHeaderParameter = endpointMethod.parameters().get(1);
		assertTrue(otherCustomHeaderParameter.isPresent());
		assertEquals("X-Other-Custom-Header", otherCustomHeaderParameter.get().name());
		assertTrue(otherCustomHeaderParameter.get().header());

		EndpointHeader customHeader = endpointMethod.headers().first("X-Custom-Header").orElse(null);
		assertNotNull(customHeader);
		assertEquals("{X-Custom-Header}", customHeader.value());

		EndpointHeader otherCustomHeader = endpointMethod.headers().first("X-Other-Custom-Header").orElse(null);
		assertNotNull(otherCustomHeader);
		assertEquals("{X-Other-Custom-Header}", otherCustomHeader.value());
	}

	@Test
	public void shouldCreateEndpointMethodWithContentTypeHeaderWhenMethodHasConsumesAnnotation() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("consumes", new Class[] { Object.class }))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/consumes", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());

		Optional<EndpointMethodParameter> bodyParameter = endpointMethod.parameters().get(0);
		assertTrue(bodyParameter.isPresent());
		assertTrue(bodyParameter.get().body());

		EndpointHeader contentTypeHeader = endpointMethod.headers().first("Content-Type").orElse(null);
		assertNotNull(contentTypeHeader);
		assertEquals("application/json", contentTypeHeader.value());
	}

	@Test
	public void shouldCreateEndpointMethodWithAcceptHeaderWhenMethodHasProducesAnnotation() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("produces", new Class[0]))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/produces", endpointMethod.path());
		assertEquals(Object.class, endpointMethod.returnType().classType());

		EndpointHeader acceptHeader = endpointMethod.headers().first("Accept").orElse(null);
		assertNotNull(acceptHeader);
		assertEquals("application/json", acceptHeader.value());
	}

	@Test
	public void shouldCreateEndpointMethodWithAcceptHeaderWhenMethodHasProducesAnnotationWithMultiplesValues() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("multipleProduces", new Class[0]))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/produces", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());

		EndpointHeader acceptHeader = endpointMethod.headers().first("Accept").orElse(null);
		assertNotNull(acceptHeader);
		assertEquals("text/plain, text/html", acceptHeader.value());
	}

	@Test
	public void shouldCreateEndpointMethodMergingMethodHeaderAnnotationsWithHeaderParameters() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("mergeHeaders", new Class[] { Object.class, String.class }))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/mergeHeaders", endpointMethod.path());
		assertEquals(Object.class, endpointMethod.returnType().classType());

		EndpointHeader contentTypeHeader = endpointMethod.headers().first("Content-Type").orElse(null);
		assertNotNull(contentTypeHeader);
		assertEquals("application/json", contentTypeHeader.value());

		EndpointHeader acceptHeader = endpointMethod.headers().first("Accept").orElse(null);
		assertNotNull(acceptHeader);
		assertEquals("application/json", acceptHeader.value());

		EndpointHeader customHeader = endpointMethod.headers().first("X-Custom-Header").orElse(null);
		assertNotNull(customHeader);
		assertEquals("{X-Custom-Header}", customHeader.value());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenMethodHasNotAPathAnnotation() throws Exception {
		jaxRsContractReader.read(new EndpointTarget(MyWrongApiWithoutPath.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenMethodHasNotAHttpMethodAnnotation() throws Exception {
		jaxRsContractReader.read(new EndpointTarget(MyWrongApiWithoutHttpMethod.class));
	}

	@Test
	public void shouldCreateEndpointMethodWhenPathAnnotationOnMethodHasNoSlashOnStart() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("pathWithoutSlash"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/path", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasCustomizedParameterNames() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("customizedNames", new Class[] { String.class }))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/{customArgumentPath}", endpointMethod.path());
		assertEquals(Void.TYPE, endpointMethod.returnType().classType());

		Optional<EndpointMethodParameter> pathParameter = endpointMethod.parameters().get(0);
		assertTrue(pathParameter.isPresent());
		assertEquals("customArgumentPath", pathParameter.get().name());
		assertTrue(pathParameter.get().path());
	}

	@Test
	public void shouldCreateEndpointMethodOfMethodWithHttpMethodMetaAnnotation() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("metaAnnotationOfHttpMethod"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("POST", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/some-method", endpointMethod.path());
		assertEquals(Void.TYPE, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodOfMethodWithQueryStringParameter() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myApiTypeTarget)
				.find(MyApiType.class.getMethod("queryString", new Class[] { String.class, int.class }))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/query", endpointMethod.path());
		assertEquals(void.class, endpointMethod.returnType().classType());

		Optional<EndpointMethodParameter> nameParameter = endpointMethod.parameters().get(0);
		assertTrue(nameParameter.isPresent());
		assertEquals("name", nameParameter.get().name());
		assertTrue(nameParameter.get().query());

		Optional<EndpointMethodParameter> ageParameter = endpointMethod.parameters().get(1);
		assertTrue(ageParameter.isPresent());
		assertEquals("age", ageParameter.get().name());
		assertTrue(ageParameter.get().query());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenMethodHasMoreThanOneBodyParameter() throws Exception {
		jaxRsContractReader.read(new EndpointTarget(MyWrongApiWithTwoBodyParameters.class));
	}

	@Test
	public void shouldCreateEndpointMethodWhenInterfaceHasAInheritance() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myInheritanceApiTarget)
				.find(MyInheritanceApiType.class.getMethod("method"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.api.com/simple", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodIsInherited() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myInheritanceApiTarget)
				.find(MyInheritanceApiType.class.getMethod("inheritedMethod"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.api.com/inherited", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWithInheritedProducesAnnotation() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myInheritanceApiTarget)
				.find(MyInheritanceApiType.class.getMethod("getWithHeaders", new Class[] { String.class }))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.api.com/getWithHeaders", endpointMethod.path());

		EndpointHeader customHeader = endpointMethod.headers().first("X-Custom-Header").orElse(null);
		assertNotNull(customHeader);
		assertEquals("{X-Custom-Header}", customHeader.value());

		EndpointHeader acceptHeader = endpointMethod.headers().first("Accept").orElse(null);
		assertNotNull(acceptHeader);
		assertEquals("application/json", acceptHeader.value());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasAGenericParameter() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myGenericSpecificApiTarget)
				.find(MySpecificApi.class.getMethod("create", new Class[] { Object.class }))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/create", endpointMethod.path());

		Optional<EndpointMethodParameter> parameter = endpointMethod.parameters().get(0);
		assertTrue(parameter.isPresent());
		assertEquals("type", parameter.get().name());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsASimpleGenericType() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myGenericSpecificApiTarget)
				.find(MySpecificApi.class.getMethod("find", new Class[] { int.class }))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/find", endpointMethod.path());
		assertEquals(MyModel.class, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsACollectionWithGenericType() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myGenericSpecificApiTarget)
				.find(MySpecificApi.class.getMethod("allAsList"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(List.class, null, MyModel.class), endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsGenericArray() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myGenericSpecificApiTarget)
				.find(MySpecificApi.class.getMethod("allAsArray"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleGenericArrayType(MyModel.class), endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsArray() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myGenericSpecificApiTarget)
				.find(MySpecificApi.class.getMethod("myModelArray"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(MyModel[].class, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMap() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myGenericSpecificApiTarget)
				.find(MySpecificApi.class.getMethod("myModelAsMap"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(Map.class, null, String.class, MyModel.class),
				endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMapWithGenericValue() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myGenericSpecificApiTarget)
				.find(MySpecificApi.class.getMethod("allAsMap"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(Map.class, null, String.class, MyModel.class),
				endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMapWithGenericKeyAndValue() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myGenericSpecificApiTarget)
				.find(MySpecificApi.class.getMethod("anyAsMap"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/any", endpointMethod.path());
		assertEquals(
				new SimpleParameterizedType(Map.class, null,
						new SimpleWildcardType(new Type[] { Number.class }, new Type[0]),
						new SimpleWildcardType(new Type[] { MyModel.class }, new Type[0])),
				endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenTargetHasEndpointUrl() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(myContextApiTarget)
				.find(MyContextApi.class.getMethod("method"))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.api.com/context/any", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenJavaMethodHasNotPathAnnotation() throws Exception {
		EndpointMethod endpointMethod = jaxRsContractReader.read(mySimpleCrudApiTarget)
				.find(MySimpleCrudApi.class.getMethod("post", MyModel.class))
					.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("http://my.api.com/context", endpointMethod.path());
		assertEquals("POST", endpointMethod.httpMethod());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenInterfaceTypeIsAnnotatedWithApplicationPathAndPathAnnotations() throws Exception {
		jaxRsContractReader.read(new EndpointTarget(MyWrongApi.class));
	}

	@ApplicationPath("http://my.api.com")
	interface MyApiType {

		@Path("/{path}")
		@GET
		public String method(@PathParam("path") String path);

		@Path("/{path}")
		@GET
		public String method(@PathParam("path") String path, @HeaderParam("Content-Type") String contentType, Object body);

		@Path("path")
		@GET
		public String pathWithoutSlash();

		@Path("/{customArgumentPath}")
		@GET
		public void customizedNames(@PathParam("customArgumentPath") String path);

		@Path("/some-method")
		@POST
		public void metaAnnotationOfHttpMethod();

		@Path("/query")
		@GET
		public void queryString(@QueryParam("name") String name, @QueryParam("age") int age);

		@Path("/headers")
		@GET
		public String headers(@HeaderParam("X-Custom-Header") String customHeader, @HeaderParam("X-Other-Custom-Header") String otherCustomHeader);

		@Path("/consumes")
		@GET
		@Consumes("application/json")
		public String consumes(Object body);

		@Path("/produces")
		@GET
		@Produces("application/json")
		public Object produces();

		@Path("/produces")
		@GET
		@Produces({"text/plain", "text/html"})
		public String multipleProduces();

		@Path("/mergeHeaders")
		@GET
		@Consumes("application/json")
		@Produces("application/json")
		public Object mergeHeaders(Object body, @HeaderParam("X-Custom-Header") String customHeader);

	}

	interface MyWrongApiWithoutPath {

		public void withoutPath();
	}

	interface MyWrongApiWithoutHttpMethod {

		@Path("/withoutHttpMethod")
		public void withoutHttpMethod();
	}

	@ApplicationPath("http://my.api.com")
	interface MyWrongApiWithTwoBodyParameters {

		@Path("/twoBodyParameters")
		@GET
		public String methodWithTwoBodyParameters(Object first, Object second);
	}

	@ApplicationPath("http://my.api.com")
	@Produces("application/json")
	interface MyBaseApiType {

		@Path("/inherited")
		@POST
		public String inheritedMethod();
	}

	interface MyInheritanceApiType extends MyBaseApiType {

		@Path("/simple")
		@GET
		public String method();

		@Path("/getWithHeaders")
		@GET
		public Object getWithHeaders(@HeaderParam("X-Custom-Header") String customHeader);
	}

	interface MyGenericApiType<T> {

		@Path("/create")
		@POST
		public void create(T type);

		@Path("/find")
		@GET
		public T find(int id);

		@Path("/all")
		@POST
		public Collection<T> all();

		@Path("/all")
		@GET
		public List<T> allAsList();

		@Path("/all")
		@GET
		public T[] allAsArray();

		@Path("/all")
		@GET
		public Map<String, T> allAsMap();

		@Path("/any")
		@GET
		public Map<? extends Number, ? extends T> anyAsMap();
	}

	@ApplicationPath("http://my.model.api")
	interface MySpecificApi extends MyGenericApiType<MyModel> {

		@Path("/update")
		@PUT
		public MyModel update(MyModel myModel);

		@Path("/all")
		@GET
		public MyModel[] myModelArray();

		@Path("/all")
		@GET
		public Map<String, MyModel> myModelAsMap();
	}

	@Path("/context")
	interface MyContextApi {

		@Path("/any")
		@GET
		public String method();
	}

	@Path("/context")
	interface MySimpleCrudApi {

		@POST
		public void post(MyModel myModel);

		@GET
		public MyModel get();

		@PUT
		public void put(MyModel myModel);

		@DELETE
		public void delete();
	}

	@ApplicationPath("http://wrong")
	@Path("/path")
	interface MyWrongApi {

		public void wrong();
	}

	class MyModel {
	}

}
