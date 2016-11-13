package com.restify.http.spring.client.call.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.request.async.WebAsyncTask;

import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.reflection.JavaType;

public class WebAsyncTaskEndpointCallExecutableFactoryTest {

	private WebAsyncTaskEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new WebAsyncTaskEndpointCallExecutableFactory<>();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsWebAsyncTask() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("webAsyncTask"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsWebAsyncTask() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithWebAsyncTaskReturnType() throws Exception {
		EndpointCallExecutable<WebAsyncTask<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("webAsyncTask")));

		String result = "future result";

		WebAsyncTask<String> future = executable.execute(() -> result, null);

		assertNotNull(future);
		assertEquals(result, future.getCallable().call());
	}

	@Test
	public void shouldCreateExecutableWithObjectReturnTypeWhenEndpointMethodReturnTypeIsNotParameterizedWebAsyncTask() throws Exception {
		EndpointCallExecutable<WebAsyncTask<String>, String> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("dumbWebAsyncTask")));
		assertEquals(JavaType.of(Object.class), executable.returnType());
	}

	interface SomeType {

		WebAsyncTask<String> webAsyncTask();

		@SuppressWarnings("rawtypes")
		WebAsyncTask dumbWebAsyncTask();

		String string();
	}
}
