package com.github.ljtfreitas.restify.http.client.call.handler.vavr;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.vavr.control.Either;

@RunWith(MockitoJUnitRunner.class)
public class EitherWithThrowableEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<String, String> delegate;

	@Mock
	private EndpointCall<String> endpointCall;

	private EitherWithThrowableEndpointCallHandlerAdapter<RuntimeException, String, String> adapter;

	private String expectedResult;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		adapter = new EitherWithThrowableEndpointCallHandlerAdapter<>();

		expectedResult = "result";

		when(endpointCall.execute())
			.thenReturn(expectedResult);

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsEither() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("either"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotEither() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldNotSupportsWhenLeftTypeOfEitherIsNotThrowable() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("eitherWithString"))));
	}

	@Test
	public void shouldReturnParameterizedTypeWithRightArgumentTypeOfEither() throws Exception {
		assertEquals(JavaType.of(String.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("either"))));
	}

	@Test
	public void shouldReturnObjectTypeWhenEitherIsNotParameterized() throws Exception {
		assertEquals(JavaType.of(Object.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbEither"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithEitherReturnType() throws Exception {
		EndpointCallHandler<Either<RuntimeException, String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("either")), delegate);

		Either<RuntimeException, String> either = handler.handle(endpointCall, null);

		assertThat(either, notNullValue());

		assertThat(either.isRight(), is(true));
		assertThat(either.contains(expectedResult), is(true));
	}

	@Test
	public void shouldReturnLeftEitherWithExceptionFromHandlerWhenEndpointCallThrowException() throws Exception {
		EndpointCallHandler<Either<RuntimeException, String>, String> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("either")), delegate);

		RuntimeException exception = new RuntimeException("ops...");

		when(endpointCall.execute())
			.thenThrow(exception);

		Either<RuntimeException, String> either = handler.handle(endpointCall, null);

		assertThat(either, notNullValue());

		assertThat(either.isLeft(), is(true));
		assertThat(either.getLeft(), is(exception));
	}

	interface SomeType {

		Either<IOException, String> either();

		Either<String, String> eitherWithString();

		@SuppressWarnings("rawtypes")
		Either dumbEither();

		String string();
	}
}
