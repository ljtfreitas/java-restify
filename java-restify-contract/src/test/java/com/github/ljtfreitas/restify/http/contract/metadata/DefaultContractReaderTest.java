package com.github.ljtfreitas.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import com.github.ljtfreitas.restify.http.contract.AcceptJson;
import com.github.ljtfreitas.restify.http.contract.BodyParameter;
import com.github.ljtfreitas.restify.http.contract.CallbackParameter;
import com.github.ljtfreitas.restify.http.contract.Delete;
import com.github.ljtfreitas.restify.http.contract.Get;
import com.github.ljtfreitas.restify.http.contract.Header;
import com.github.ljtfreitas.restify.http.contract.HeaderParameter;
import com.github.ljtfreitas.restify.http.contract.JsonContent;
import com.github.ljtfreitas.restify.http.contract.Method;
import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.contract.Path;
import com.github.ljtfreitas.restify.http.contract.PathParameter;
import com.github.ljtfreitas.restify.http.contract.Post;
import com.github.ljtfreitas.restify.http.contract.Put;
import com.github.ljtfreitas.restify.http.contract.QueryParameters;
import com.github.ljtfreitas.restify.http.contract.Version;
import com.github.ljtfreitas.restify.reflection.SimpleGenericArrayType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;
import com.github.ljtfreitas.restify.reflection.SimpleWildcardType;

public class DefaultContractReaderTest {

	private EndpointTarget myApiTypeTarget;

	private EndpointTarget myInheritanceApiTarget;

	private EndpointTarget myGenericSpecificApiTarget;

	private EndpointTarget myContextApiTarget;

	private EndpointTarget mySimpleCrudApiTarget;

	private EndpointTarget myVersionedApiTarget;

	private DefaultContractReader restifyContractReader;

	@Before
	public void setup() {
		myApiTypeTarget = new EndpointTarget(MyApiType.class);

		myInheritanceApiTarget = new EndpointTarget(MyInheritanceApiType.class);

		myGenericSpecificApiTarget = new EndpointTarget(MySpecificApi.class);

		myContextApiTarget = new EndpointTarget(MyContextApi.class, "http://my.api.com");

		mySimpleCrudApiTarget = new EndpointTarget(MySimpleCrudApi.class, "http://my.api.com");

		myVersionedApiTarget = new EndpointTarget(MyVersionedApi.class);

		restifyContractReader = new DefaultContractReader();
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasSingleParameter() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myApiTypeTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyApiType.class.getMethod("method", new Class[] { String.class }))
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
		EndpointMethods endpointMethods = restifyContractReader.read(myApiTypeTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyApiType.class.getMethod("method", new Class[] { String.class, String.class, Object.class }))
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
		assertEquals("contentType", headerParameter.get().name());
		assertTrue(headerParameter.get().header());

		Optional<EndpointMethodParameter> bodyParameter = endpointMethod.parameters().get(2);
		assertTrue(bodyParameter.isPresent());
		assertEquals("body", bodyParameter.get().name());
		assertTrue(bodyParameter.get().body());
	}

	@Test
	public void shouldCreateEndpointMethodWhenPathAnnotationOnMethodHasNoSlashOnStart() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myApiTypeTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyApiType.class.getMethod("pathWithoutSlash"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/path", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodMergingEndpointHeadersDeclaredOnTypeWithDeclaredOnMethod() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myApiTypeTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyApiType.class.getMethod("mergeHeaders"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

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
	public void shouldCreateEndpointMethodWithEndpointHeadersMetaAnnotation() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myApiTypeTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyApiType.class.getMethod("metaHeaders"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/metaHeaders", endpointMethod.path());
		assertEquals(String.class, endpointMethod.returnType().classType());

		Optional<EndpointHeader> contentTypeHeader = endpointMethod.headers().first("Content-Type");
		assertTrue(contentTypeHeader.isPresent());
		assertEquals("application/json", contentTypeHeader.get().value());

		Optional<EndpointHeader> userAgentHeader = endpointMethod.headers().first("User-Agent");
		assertTrue(userAgentHeader.isPresent());
		assertEquals("Restify-Agent", userAgentHeader.get().value());

		Optional<EndpointHeader> acceptHeader = endpointMethod.headers().first("Accept");
		assertTrue(acceptHeader.isPresent());
		assertEquals("application/json", acceptHeader.get().value());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasCustomizedParameterNames() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myApiTypeTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyApiType.class.getMethod("customizedNames", new Class[] { String.class, String.class }))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/{customArgumentPath}", endpointMethod.path());
		assertEquals(Void.TYPE, endpointMethod.returnType().classType());

		Optional<EndpointMethodParameter> pathParameter = endpointMethod.parameters().get(0);
		assertTrue(pathParameter.isPresent());
		assertEquals("customArgumentPath", pathParameter.get().name());
		assertTrue(pathParameter.get().path());

		Optional<EndpointMethodParameter> headerParameter = endpointMethod.parameters().get(1);
		assertTrue(headerParameter.isPresent());
		assertEquals("customArgumentContentType", headerParameter.get().name());
		assertTrue(headerParameter.get().header());
	}

	@Test
	public void shouldCreateEndpointMethodOfMethodWithHttpMethodMetaAnnotation() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myApiTypeTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyApiType.class.getMethod("metaAnnotationOfHttpMethod"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("POST", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/some-method", endpointMethod.path());
		assertEquals(Void.TYPE, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodOfMethodWithQueryStringParameter() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myApiTypeTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyApiType.class.getMethod("queryString", new Class[] { Parameters.class }))
				.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/query", endpointMethod.path());
		assertEquals(Void.class, endpointMethod.returnType().classType());

		Optional<EndpointMethodParameter> queryStringParameter = endpointMethod.parameters().get(0);
		assertTrue(queryStringParameter.isPresent());
		assertEquals("parameters", queryStringParameter.get().name());
		assertTrue(queryStringParameter.get().query());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenMethodHasMoreThanOneBodyParameter() throws Exception {
		new DefaultContractReader().read(new EndpointTarget(MyApiTypeWithWrongBodyParameter.class));
	}

	@Test
	public void shouldCreateAsyncEndpointMethodWhenMethodHasOneCallback() throws Exception {
		EndpointMethods endpointMethods = new DefaultContractReader().read(myApiTypeTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyApiType.class.getMethod("async", new Class[] { AsyncCallback.class }))
				.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/async", endpointMethod.path());
		assertTrue(endpointMethod.returnType().voidType());
		assertTrue(endpointMethod.runnableAsync());

		Optional<EndpointMethodParameter> callbackParameter = endpointMethod.parameters().get(0);
		assertTrue(callbackParameter.isPresent());
		assertEquals("callback", callbackParameter.get().name());
		assertTrue(callbackParameter.get().callback());
		assertEquals(new SimpleParameterizedType(AsyncCallback.class, this.getClass(), String.class), callbackParameter.get().javaType().unwrap());

		assertFalse(endpointMethod.parameters().callbacks().isEmpty());
	}

	@Test
	public void shouldCreateAsyncEndpointMethodWhenMethodHasMultiplesCallbacks() throws Exception {
		EndpointMethods endpointMethods = new DefaultContractReader().read(myApiTypeTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyApiType.class.getMethod("async", new Class[] { AsyncSuccessCallback.class, AsyncFailureCallback.class }))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("GET", endpointMethod.httpMethod());
		assertEquals("http://my.api.com/async", endpointMethod.path());
		assertTrue(endpointMethod.returnType().voidType());
		assertTrue(endpointMethod.runnableAsync());

		Optional<EndpointMethodParameter> successCallbackParameter = endpointMethod.parameters().get(0);
		assertTrue(successCallbackParameter.isPresent());
		assertEquals("successCallback", successCallbackParameter.get().name());
		assertTrue(successCallbackParameter.get().callback());
		assertEquals(new SimpleParameterizedType(AsyncSuccessCallback.class, this.getClass(), String.class), successCallbackParameter.get().javaType().unwrap());

		Optional<EndpointMethodParameter> failureCallbackParameter = endpointMethod.parameters().get(1);
		assertTrue(failureCallbackParameter.isPresent());
		assertEquals("failureCallback", failureCallbackParameter.get().name());
		assertTrue(failureCallbackParameter.get().callback());
		assertEquals(AsyncFailureCallback.class, failureCallbackParameter.get().javaType().unwrap());

		assertFalse(endpointMethod.parameters().callbacks().isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenMethodHasMoreOneCallbackParameterOfSameType() throws Exception {
		new DefaultContractReader().read(new EndpointTarget(MyApiTypeWithWrongCallbackParameter.class));
	}

	@Test
	public void shouldCreateEndpointMethodWhenInterfaceHasAInheritance() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myInheritanceApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyInheritanceApiType.class.getMethod("method"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.api.com/simple", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodIsInherited() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myInheritanceApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyInheritanceApiType.class.getMethod("inheritedMethod"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.api.com/inherited", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodHasAGenericParameter() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myGenericSpecificApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MySpecificApi.class.getMethod("create", new Class[] { Object.class }))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/create", endpointMethod.path());

		Optional<EndpointMethodParameter> parameter = endpointMethod.parameters().get(0);
		assertTrue(parameter.isPresent());
		assertEquals("type", parameter.get().name());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsASimpleGenericType() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myGenericSpecificApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MySpecificApi.class.getMethod("find", new Class[] { int.class }))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/find", endpointMethod.path());
		assertEquals(MyModel.class, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsACollectionWithGenericType() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myGenericSpecificApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MySpecificApi.class.getMethod("allAsList"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(List.class, null, MyModel.class), endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsGenericArray() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myGenericSpecificApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MySpecificApi.class.getMethod("allAsArray"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleGenericArrayType(MyModel.class), endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsArray() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myGenericSpecificApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MySpecificApi.class.getMethod("myModelArray"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(MyModel[].class, endpointMethod.returnType().classType());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMap() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myGenericSpecificApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MySpecificApi.class.getMethod("myModelAsMap"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(Map.class, null, String.class, MyModel.class),
				endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMapWithGenericValue() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myGenericSpecificApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MySpecificApi.class.getMethod("allAsMap"))
				.orElseThrow(() -> new IllegalStateException("Method not found..."));

		assertEquals("http://my.model.api/all", endpointMethod.path());
		assertEquals(new SimpleParameterizedType(Map.class, null, String.class, MyModel.class),
				endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateEndpointMethodWhenMethodReturnTypeIsMapWithGenericKeyAndValue() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myGenericSpecificApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MySpecificApi.class.getMethod("anyAsMap"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("http://my.model.api/any", endpointMethod.path());
		assertEquals(
				new SimpleParameterizedType(Map.class, null,
						new SimpleWildcardType(new Type[] { Number.class }, new Type[0]),
						new SimpleWildcardType(new Type[] { MyModel.class }, new Type[0])),
				endpointMethod.returnType().unwrap());
	}

	@Test
	public void shouldCreateAsyncEndpointMethodWhenMethodHasOneCallbackWithGenericType() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myGenericSpecificApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MySpecificApi.class.getMethod("async", AsyncCallback.class))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("http://my.model.api/async", endpointMethod.path());
		assertEquals("GET", endpointMethod.httpMethod());
		assertTrue(endpointMethod.returnType().voidType());
		assertTrue(endpointMethod.runnableAsync());

		Optional<EndpointMethodParameter> callbackParameter = endpointMethod.parameters().get(0);
		assertTrue(callbackParameter.isPresent());
		assertEquals("callback", callbackParameter.get().name());
		assertTrue(callbackParameter.get().callback());
		assertEquals(new SimpleParameterizedType(AsyncCallback.class, this.getClass(), MyModel.class), callbackParameter.get().javaType().unwrap());

		assertFalse(endpointMethod.parameters().callbacks().isEmpty());
	}

	@Test
	public void shouldCreateEndpointMethodWhenTargetHasEndpointUrl() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(myContextApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyContextApi.class.getMethod("method"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("http://my.api.com/context/any", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenJavaMethodHasNotPathAnnotation() throws Exception {
		EndpointMethods endpointMethods = restifyContractReader.read(mySimpleCrudApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MySimpleCrudApi.class.getMethod("post", MyModel.class))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("http://my.api.com/context", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenInterfaceHasDynamicPath() throws Exception {
		restifyContractReader = new DefaultContractReader(new DynamicExpressionResolver());

		EndpointTarget dynamicApiTarget = new EndpointTarget(MyDynamicApi.class);

		EndpointMethods endpointMethods = restifyContractReader.read(dynamicApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(MyDynamicApi.class.getMethod("method"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("http://localhost:8080/any", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWhenTargetEndpointIsDynamic() throws Exception {
		restifyContractReader = new DefaultContractReader(new DynamicExpressionResolver());

		EndpointTarget dynamicApiTarget = new EndpointTarget(OtherDynamicApi.class, "@{api.endpoint}");

		EndpointMethods endpointMethods = restifyContractReader.read(dynamicApiTarget);

		EndpointMethod endpointMethod = endpointMethods.find(OtherDynamicApi.class.getMethod("method"))
			.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("http://localhost:8080/context/any", endpointMethod.path());
	}

	@Test
	public void shouldCreateEndpointMethodWithVersionWhenTypeIsVersioned() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myVersionedApiTarget)
			.find(MyVersionedApi.class.getMethod("versionOne"))
				.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("http://my.api.com/v1/model", endpointMethod.path());

		assertTrue(endpointMethod.version().isPresent());
		assertEquals("v1", endpointMethod.version().get());
	}

	@Test
	public void shouldCreateEndpointMethodWithMethodVersionWhenVersionAnnotationIsPresentOnMethod() throws Exception {
		EndpointMethod endpointMethod = restifyContractReader.read(myVersionedApiTarget)
			.find(MyVersionedApi.class.getMethod("versionTwo"))
				.orElseThrow(() -> new IllegalStateException("Method not found..."));;

		assertEquals("http://my.api.com/v2/model", endpointMethod.path());

		assertTrue(endpointMethod.version().isPresent());
		assertEquals("v2", endpointMethod.version().get());
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

		@Path("/async")
		@Get
		public void async(@CallbackParameter AsyncCallback<String> callback);

		@Path("/async")
		@Get
		public void async(@CallbackParameter AsyncSuccessCallback<String> successCallback,
				@CallbackParameter AsyncFailureCallback failureCallback);

		@Path("/metaHeaders")
		@Method("GET")
		@JsonContent
		@AcceptJson
		@Header(name = "User-Agent", value = "Restify-Agent")
		public String metaHeaders();
	}

	@Path("http://my.api.com")
	interface MyApiTypeWithWrongBodyParameter {

		@Path("/twoBodyParameters")
		@Get
		public String methodWithTwoBodyParameters(@BodyParameter Object first, @BodyParameter Object second);
	}

	@Path("http://my.api.com")
	interface MyApiTypeWithWrongCallbackParameter {

		@Path("/asyncWithTwoCallbacks")
		@Get
		public void asyncWithTwoCallbacks(@CallbackParameter AsyncCallback<String> first, @CallbackParameter AsyncCallback<String> second);
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

		@Path("/async")
		@Get
		public void async(@CallbackParameter AsyncCallback<T> callback);
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

	@Path("/context")
	interface MySimpleCrudApi {

		@Post
		public void post(MyModel model);

		@Get
		public MyModel get();

		@Put
		public void put(MyModel model);

		@Delete
		public void delete();
	}

	@Path("@{api.endpoint}")
	interface MyDynamicApi {

		@Path("/any")
		@Get
		public String method();
	}

	@Path("/context")
	interface OtherDynamicApi {

		@Path("/any")
		@Get
		public String method();
	}

	@Path("http://my.api.com")
	@Version("v1")
	interface MyVersionedApi {

		@Path("/model")
		@Get
		public MyModel versionOne();

		@Path("/model")
		@Get
		@Version("v2")
		public MyModel versionTwo();
	}

	class MyModel {
	}

	private class DynamicExpressionResolver implements ContractExpressionResolver {

		private final Pattern dynamicPattern = Pattern.compile("\\@\\{([a-zA-Z\\.]+)\\}");

		private final Properties properties = new Properties();

		private DynamicExpressionResolver() {
			properties.put("api.endpoint", "http://localhost:8080/");
		}

		@Override
		public String resolve(String expression) {
			Matcher matcher = dynamicPattern.matcher(expression);

			if (matcher.find()) {
				StringBuffer builder = new StringBuffer();

				matcher.appendReplacement(builder, properties.getProperty(matcher.group(1)));

				return builder.toString();

			} else {
				return expression;
			}
		}
	}

	private interface AsyncCallback<T> {
	}

	private interface AsyncSuccessCallback<T> {
	}

	private interface AsyncFailureCallback {
	}
}
