package com.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.EndpointMethodParameter;
import com.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.restify.http.contract.metadata.EndpointMethodParameters;
import com.restify.http.contract.metadata.SimpleEndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

public class CompletableFutureCallbackEndpointCallExecutableFactoryTest {

	private CompletableFutureCallbackEndpointCallExecutableFactory<String> factory;

	@Before
	public void setup() {
		factory = new CompletableFutureCallbackEndpointCallExecutableFactory<>(r -> r.run());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithCallbackParameter() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(BiConsumer.class, null, String.class, Throwable.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("futureWithCallback", BiConsumer.class), parameters);

		assertTrue(factory.supports(endpointMethod));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodNotIsRunnableAsync() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("sync"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithBiConsumerCallbackParameter() throws Exception {
		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(BiConsumer.class, null, String.class, Throwable.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		SimpleEndpointMethod endpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("futureWithCallback", BiConsumer.class), parameters);

		EndpointCallExecutable<Void, String> executable = factory.create(endpointMethod);

		String result = "future result";
		BiConsumer<String, Throwable> callbackArgument = ((r, t) -> assertEquals("other", r));

		executable.execute(() -> result, new Object[]{callbackArgument});

		assertEquals(JavaType.of(String.class), executable.returnType());
	}

	interface SomeType {

		void futureWithCallback(BiConsumer<String, Throwable> callback);

		String sync();
	}
}
