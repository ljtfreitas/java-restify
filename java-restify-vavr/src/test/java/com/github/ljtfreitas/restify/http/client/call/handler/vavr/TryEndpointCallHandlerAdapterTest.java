package com.github.ljtfreitas.restify.http.client.call.handler.vavr;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.vavr.control.Try;

@RunWith(MockitoJUnitRunner.class)
public class TryEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	@Mock
	private EndpointCall<String> endpointCall;

	private TryEndpointCallHandlerAdapter<String, String> adapter;

	private String expectedResult;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		adapter = new TryEndpointCallHandlerAdapter<>();

		expectedResult = "result";

		when(endpointCall.execute())
			.thenReturn(expectedResult);

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsTry() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("aTry"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotTry() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnParameterizedTypeWithArgumentTypeOfTry() throws Exception {
		assertEquals(JavaType.of(String.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("aTry"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenTryIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbTry"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithTryReturnType() throws Exception {
		EndpointCallHandler<Try<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("aTry")), delegate);

		Try<String> aTry = handler.handle(endpointCall, null);

		assertThat(aTry, notNullValue());

		assertThat(aTry.isSuccess(), is(true));
		assertThat(aTry.get(), equalTo(expectedResult));
	}

	@Test
	public void shouldReturnFailureTryFromHandlerWhenEndpointCallThrowException() throws Exception {
		EndpointCallHandler<Try<String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("aTry")), delegate);

		RuntimeException exception = new RuntimeException("ops...");

		when(endpointCall.execute())
			.thenThrow(exception);

		Try<String> aTry = handler.handle(endpointCall, null);

		assertThat(aTry, notNullValue());

		assertThat(aTry.isFailure(), is(true));
		assertThat(aTry.getCause(), is(exception));
	}

	interface SomeType {

		Try<String> aTry();

		@SuppressWarnings("rawtypes")
		Try dumbTry();

		String string();
	}
}
