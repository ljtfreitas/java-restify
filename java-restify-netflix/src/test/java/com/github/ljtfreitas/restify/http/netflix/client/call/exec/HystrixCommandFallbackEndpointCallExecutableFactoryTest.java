package com.github.ljtfreitas.restify.http.netflix.client.call.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

@RunWith(MockitoJUnitRunner.class)
public class HystrixCommandFallbackEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private HystrixCommandFallbackEndpointCallExecutableFactory<String, String, SomeType> factory;

	@Spy
	private SomeFallback fallback = new SomeFallback();

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		factory = new HystrixCommandFallbackEndpointCallExecutableFactory<>(fallback);

		when(delegate.execute(notNull(EndpointCall.class), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsHystrixCommand() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("command"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotHystrixCommand() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateExecutableFromEndpointMethodWithHystrixCommandReturnType() throws Exception {
		EndpointCallExecutable<HystrixCommand<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		String result = "result";

		HystrixCommand<String> hystrixCommand = executable.execute(() -> result, null);

		assertEquals(result, hystrixCommand.execute());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(notNull(EndpointCall.class), anyVararg());
	}

	@Test
	public void shouldReturnObjectTypeWhenHystrixCommandIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class), factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbCommand"))));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnFallbackValueWhenHystrixCommandExecutionThrowException() throws Exception {
		EndpointCallExecutable<HystrixCommand<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("command")), delegate);

		HystrixCommand<String> hystrixCommand = executable.execute(() -> {throw new RuntimeException("oooh!");}, null);

		assertEquals("fallback", hystrixCommand.execute());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(notNull(EndpointCall.class), anyVararg());
		verify(fallback).command();
	}

	interface SomeType {

		HystrixCommand<String> command();

		@SuppressWarnings("rawtypes")
		HystrixCommand dumbCommand();

		String string();
	}

	private class SomeFallback implements SomeType {

		@Override
		public HystrixCommand<String> command() {
			return new HystrixCommand<String>(HystrixCommandGroupKey.Factory.asKey("fallback")) {
				@Override
				protected String run() throws Exception {
					return "fallback";
				}
			};
		}

		@SuppressWarnings("rawtypes")
		@Override
		public HystrixCommand dumbCommand() {
			return new HystrixCommand(HystrixCommandGroupKey.Factory.asKey("fallback")) {
				@Override
				protected Object run() throws Exception {
					return "fallback";
				}
			};
		}

		@Override
		public String string() {
			return "string";
		}
	}
}
