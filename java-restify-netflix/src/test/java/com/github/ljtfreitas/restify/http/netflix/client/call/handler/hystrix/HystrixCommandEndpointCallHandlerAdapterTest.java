package com.github.ljtfreitas.restify.http.netflix.client.call.handler.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;

@RunWith(MockitoJUnitRunner.class)
public class HystrixCommandEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	private HystrixCommandEndpointCallHandlerAdapter<String, String> adapter;

	@Before
	public void setup() {
		adapter = new HystrixCommandEndpointCallHandlerAdapter<>();

		when(delegate.handle(any(), any()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsHystrixCommand() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("command"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotHystrixCommand() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateHandlerFromEndpointMethodWithHystrixCommandReturnType() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		String result = "result";

		HystrixCommand<String> hystrixCommand = handler.handle(() -> result, null);

		assertEquals(result, hystrixCommand.execute());
		assertEquals(delegate.returnType(), handler.returnType());

		verify(delegate).handle(notNull(EndpointCall.class), any());
	}

	@Test
	public void shouldReturnObjectTypeWhenHystrixCommandIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbCommand"))));
	}

	@Test(expected = HystrixRuntimeException.class)
	public void shouldPropagateExceptionWhenHystrixCommandThrowsException() throws Exception {
		EndpointCallHandler<HystrixCommand<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		HystrixCommand<String> hystrixCommand = handler.handle(() -> {throw new RuntimeException("oooh!");}, null);

		hystrixCommand.execute();
	}

	interface SomeType {

		HystrixCommand<String> command();

		@SuppressWarnings("rawtypes")
		HystrixCommand dumbCommand();

		String string();
	}
}
