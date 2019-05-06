package com.github.ljtfreitas.restify.http.call.handler;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class StreamEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<Collection<String>, Collection<String>> delegate;

	private StreamEndpointCallHandlerAdapter<String> adapter;

	@Before
	public void setup() {
		adapter = new StreamEndpointCallHandlerAdapter<>();

		when(delegate.handle(any(), anyVararg()))
			.then(invocation -> invocation.getArgumentAt(0, EndpointCall.class).execute());

		when(delegate.returnType())
			.thenReturn(JavaType.parameterizedType(Collection.class, null, String.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsStream() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("stream"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotStream() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnCollectionParameterizedWithArgumentTypeOfStream() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, null, String.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("stream"))));
	}

	@Test
	public void shouldReturnCollectionParameterizedWithObjectWhenStreamIsNotParameterized() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, null, Object.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbStream"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithIteratorReturnType() throws Exception {
		EndpointCallHandler<Stream<String>, Collection<String>> handler = adapter.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("stream")), delegate);

		String result = "result";

		Stream<String> stream = handler.handle(() -> Arrays.asList(result), null);

		assertEquals(JavaType.parameterizedType(Collection.class, null, String.class), handler.returnType());

		assertThat(stream.anyMatch(result::equals), is(true));
	}

	private interface SomeType {

		Stream<String> stream();

		@SuppressWarnings("rawtypes")
		Stream dumbStream();

		String string();
	}

}
