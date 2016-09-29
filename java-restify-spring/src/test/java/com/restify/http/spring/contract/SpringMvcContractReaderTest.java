package com.restify.http.spring.contract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.restify.http.contract.metadata.EndpointHeader;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointTarget;
import com.restify.http.contract.metadata.reflection.SimpleGenericArrayType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;
import com.restify.http.contract.metadata.reflection.SimpleWildcardType;
import com.restify.http.spring.contract.metadata.EndpointParameterExpressionResolver;

@RunWith(MockitoJUnitRunner.class)
public class SpringMvcContractReaderTest {

	private EndpointTarget myApiTypeTarget;

	private EndpointTarget myInheritanceApiTarget;

	private EndpointTarget myGenericSpecificApiTarget;

	private EndpointTarget myContextApiTarget;

	@Mock
	private EndpointParameterExpressionResolver expressionResolverMock;

	@InjectMocks
	private SpringMvcContractReader springMvcContractReader;

	@Before
	public void setup() {
		myApiTypeTarget = new EndpointTarget(MyApiType.class);

		myInheritanceApiTarget = new EndpointTarget(MyInheritanceApiType.class);

		myGenericSpecificApiTarget = new EndpointTarget(MySpecificApi.class);

		myContextApiTarget = new EndpointTarget(MyContextApi.class, "http://my.api.com");

		when(expressionResolverMock.resolve(anyString()))
			.then(returnsFirstArg());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasNoParameter() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("method"));

		assertEquals("http://my.api.com/path", endpointMethod.path());
		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals(String.class, endpointMethod.returnType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasSingleParameter() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("method", new Class[] { String.class }));

		assertEquals("http://my.api.com/{path}", endpointMethod.path());
		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals(String.class, endpointMethod.returnType());

		Optional<EndpointMethodParameter> parameter = endpointMethod.parameters().find("path");
		assertTrue(parameter.isPresent());

		assertEquals(0, parameter.get().position());
		assertTrue(parameter.get().ofPath());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasMultiplesParameters() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("method", new Class[] { String.class, String.class, Object.class }));

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
		EndpointMethod endpointMethod = springMvcContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("pathWithoutSlash"));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/path", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType());
	}

	@Test
	public void shouldCreateEndpointMethodMergingEndpointHeadersDeclaredOnTypeWithDeclaredOnMethod() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("mergeHeaders"));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/mergeHeaders", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType());

		Optional<EndpointHeader> myTypeHeader = endpointMethod.headers().first("X-My-Type");
		assertTrue(myTypeHeader.isPresent());
		assertEquals("MyApiType", myTypeHeader.get().value());

		Optional<EndpointHeader> contentTypeHeader = endpointMethod.headers().first("Content-Type");
		assertTrue(contentTypeHeader.isPresent());
		assertEquals("application/json", contentTypeHeader.get().value());

		Optional<EndpointHeader> acceptHeader = endpointMethod.headers().first("Accept");
		assertTrue(acceptHeader.isPresent());
		assertEquals("application/json, application/xml", acceptHeader.get().value());

		Optional<EndpointHeader> userAgentHeader = endpointMethod.headers().first("User-Agent");
		assertTrue(userAgentHeader.isPresent());
		assertEquals("Spring-Restify-Agent", userAgentHeader.get().value());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasCustomizedParameterNames() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("customizedNames", new Class[] { String.class, String.class }));

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
		EndpointMethod endpointMethod = springMvcContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("metaAnnotationOfHttpMethod"));

		assertEquals("POST", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/some-method", endpointMethod.path());
		assertEquals(Void.TYPE, endpointMethod.returnType());
	}

	@Test
	public void shouldReadMetadataOfMethodWithQueryStringParameter() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("queryString", new Class[] { Map.class }));

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
		springMvcContractReader.read(myApiTypeTarget,
				MyApiType.class.getMethod("methodWithTwoBodyParameters", new Class[] { Object.class, Object.class }));
	}

	@Test
	public void shouldCreateEndpointMethodWhenInterfaceHasAInheritance() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myInheritanceApiTarget,
				MyInheritanceApiType.class.getMethod("method"));

		assertEquals("http://my.api.com/context/simple", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodIsInherited() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myInheritanceApiTarget,
				MyInheritanceApiType.class.getMethod("inheritedMethod"));

		assertEquals("http://my.api.com/context/inherited", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasAGenericParameter() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("create", new Class[] { Object.class }));

		assertEquals("http://my.model.api/create", endpointMethod.path());

		Optional<EndpointMethodParameter> parameter = endpointMethod.parameters().get(0);
		assertTrue(parameter.isPresent());
		assertEquals("type", parameter.get().name());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsASimpleGenericType() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("find", new Class[] { int.class }));

		assertEquals("http://my.model.api/find", endpointMethod.path());
		assertEquals(MyModel.class, endpointMethod.returnType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsACollectionWithGenericType() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("allAsList"));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(List.class, null, MyModel.class), endpointMethod.returnType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsGenericArray() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("allAsArray"));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleGenericArrayType(MyModel.class), endpointMethod.returnType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsArray() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("myModelArray"));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(MyModel[].class, endpointMethod.returnType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMap() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("myModelAsMap"));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(Map.class, null, String.class, MyModel.class),
				endpointMethod.returnType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMapWithGenericValue() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("allAsMap"));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(Map.class, null, String.class, MyModel.class),
				endpointMethod.returnType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMapWithGenericKeyAndValue() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myGenericSpecificApiTarget,
				MySpecificApi.class.getMethod("anyAsMap"));

		assertEquals("http://my.model.api/any", endpointMethod.path());
		assertEquals(
				new SimpleParameterizedType(Map.class, null,
						new SimpleWildcardType(new Type[] { Number.class }, new Type[0]),
						new SimpleWildcardType(new Type[] { MyModel.class }, new Type[0])),
				endpointMethod.returnType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenTargetHasEndpointUrl() throws Exception {
		EndpointMethod endpointMethod = springMvcContractReader.read(myContextApiTarget,
				MyContextApi.class.getMethod("method"));

		assertEquals("http://my.api.com/context/any", endpointMethod.path());
	}

	@RequestMapping(path = "http://my.api.com", headers = "X-My-Type=MyApiType")
	interface MyApiType {

		@RequestMapping(path = "/path", method = RequestMethod.GET)
		public String method();

		@RequestMapping(path = "/{path}", method = RequestMethod.GET)
		public String method(@PathVariable String path);

		@RequestMapping(path = "/{path}", method = RequestMethod.GET, produces = "{contentType}")
		public String method(@PathVariable String path, @RequestHeader String contentType, @RequestBody Object body);

		@RequestMapping(path = "path", method = RequestMethod.GET)
		public String pathWithoutSlash();

		@RequestMapping(path = "/mergeHeaders", method = RequestMethod.GET, produces = "application/json",
				consumes = {"application/json", "application/xml" }, headers = "User-Agent=Spring-Restify-Agent")
		public String mergeHeaders();

		@RequestMapping(value = "/{customArgumentPath}", method = RequestMethod.GET,
				produces = "{customArgumentContentType}")
		public void customizedNames(@PathVariable("customArgumentPath") String path,
				@RequestHeader("customArgumentContentType") String contentType);

		@PostMapping("/some-method")
		public void metaAnnotationOfHttpMethod();

		@RequestMapping(value = "/query", method = RequestMethod.GET)
		public Void queryString(@RequestParam Map<String, String> parameters);

		@RequestMapping("/twoBodyParameters")
		public String methodWithTwoBodyParameters(@RequestBody Object first, @RequestBody Object second);
	}

	@RequestMapping("http://my.api.com")
	interface MyBaseApiType {

		@PostMapping("/inherited")
		public String inheritedMethod();
	}

	@RequestMapping("/context")
	interface MyInheritanceApiType extends MyBaseApiType {

		@GetMapping("/simple")
		public String method();
	}

	interface MyGenericApiType<T> {

		@PostMapping("/create")
		public void create(T type);

		@GetMapping("/find")
		public T find(int id);

		@GetMapping("/all")
		public Collection<T> all();

		@GetMapping("/all")
		public List<T> allAsList();

		@GetMapping("/all")
		public T[] allAsArray();

		@GetMapping("/all")
		public Map<String, T> allAsMap();

		@GetMapping("/any")
		public Map<? extends Number, ? extends T> anyAsMap();
	}

	@RequestMapping("http://my.model.api")
	interface MySpecificApi extends MyGenericApiType<MyModel> {

		@PutMapping("/update")
		public MyModel update(MyModel myModel);

		@GetMapping("/all")
		public MyModel[] myModelArray();

		@GetMapping("/all")
		public Map<String, MyModel> myModelAsMap();
	}

	@RequestMapping("/context")
	interface MyContextApi {

		@GetMapping("/any")
		public String method();
	}

	class MyModel {
	}
}
