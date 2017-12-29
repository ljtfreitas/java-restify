package com.github.ljtfreitas.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.http.client.call.exec.jdk.CompletableFutureCallbackEndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class CompletableFutureCallbackEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private CompletableFutureCallbackEndpointCallExecutableFactory<String, String> factory;

	private SimpleEndpointMethod futureWithCallbackEndpointMethod;

	@Before
	public void setup() throws Exception {
		factory = new CompletableFutureCallbackEndpointCallExecutableFactory<>(r -> r.run());

		when(delegate.execute(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));

		EndpointMethodParameters parameters = new EndpointMethodParameters();
		parameters.put(new EndpointMethodParameter(0, "callback",
				new SimpleParameterizedType(BiConsumer.class, null, String.class, Throwable.class), EndpointMethodParameterType.ENDPOINT_CALLBACK));

		futureWithCallbackEndpointMethod = new SimpleEndpointMethod(SomeType.class.getMethod("futureWithCallback", BiConsumer.class), parameters);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodIsRunnableAsyncWithCallbackParameter() throws Exception {
		assertTrue(factory.supports(futureWithCallbackEndpointMethod));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodIsNotRunnableAsync() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("sync"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfConsumerCallbackParameter() throws Exception {
		assertEquals(JavaType.of(String.class), factory.returnType(futureWithCallbackEndpointMethod));
	}

	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithBiConsumerCallbackParameter() throws Exception {
		EndpointCallExecutable<Void, String> executable = factory.create(futureWithCallbackEndpointMethod, delegate);

		String result = "future result";
		BiConsumer<String, Throwable> callbackArgument = ((r, t) -> assertEquals("other", r));

		executable.execute(() -> result, new Object[]{callbackArgument});

		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		void futureWithCallback(BiConsumer<String, Throwable> callback);

		String sync();
	}
}
