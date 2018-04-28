package com.github.ljtfreitas.restify.http.client.call.exec.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.async.AsyncEndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.SimpleEndpointMethod;
import com.github.ljtfreitas.restify.http.client.call.exec.async.AsyncEndpointCallExecutable;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameter.EndpointMethodParameterType;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameters;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class CompletableFutureCallbackEndpointCallExecutableFactoryTest {

	@Mock
	private AsyncEndpointCallExecutable<String, String> delegate;

	@Mock
	private AsyncEndpointCall<String> asyncEndpointCall;

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

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointRunnableAsyncMethodWithBiConsumerCallbackParameter() throws Exception {
		AsyncEndpointCallExecutable<Void, String> executable = factory.createAsync(futureWithCallbackEndpointMethod, delegate);

		String result = "future result";

		when(asyncEndpointCall.executeAsync()).thenReturn(CompletableFuture.completedFuture(result));

		BiConsumer<String, Throwable> callback = mock(BiConsumer.class);

		executable.executeAsync(asyncEndpointCall, new Object[]{callback});

		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());

		verify(callback).accept(eq(result), isNull(Throwable.class));
	}

	interface SomeType {

		void futureWithCallback(BiConsumer<String, Throwable> callback);

		String sync();
	}
}
