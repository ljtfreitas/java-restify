package com.restify.http.spring.client.call.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.context.request.async.DeferredResult;

import com.restify.http.client.call.EndpointCall;
import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.contract.metadata.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class DeferredResultEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<String, String> delegate;

	private DeferredResultEndpointCallExecutableFactory<String, String> factory;

	@Before
	public void setup() {
		factory = new DeferredResultEndpointCallExecutableFactory<>(r -> r.run());

		when(delegate.execute(any(), anyVararg()))
			.then((invocation) -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.of(String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsDeferredResult() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("deferredResult"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsDeferredResult() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnArgumentTypeOfDeferredResult() throws Exception {
		assertEquals(JavaType.of(String.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("deferredResult"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenDeferredResultIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbDeferredResult"))));
	}

	@Test
	public void shouldCreateExecutableFromEndpointMethodWithDeferredResultReturnType() throws Exception {
		EndpointCallExecutable<DeferredResult<String>, String> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("deferredResult")), delegate);

		String result = "deferred result";

		DeferredResult<String> deferredResult = executable.execute(() -> result, null);

		assertEquals(result, deferredResult.getResult());
		assertEquals(delegate.returnType(), executable.returnType());

		verify(delegate).execute(any(), anyVararg());
	}

	interface SomeType {

		DeferredResult<String> deferredResult();

		@SuppressWarnings("rawtypes")
		DeferredResult dumbDeferredResult();

		String string();
	}

}
