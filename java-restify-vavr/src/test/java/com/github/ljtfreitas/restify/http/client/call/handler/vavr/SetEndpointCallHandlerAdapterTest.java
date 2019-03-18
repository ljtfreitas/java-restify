package com.github.ljtfreitas.restify.http.client.call.handler.vavr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.SetEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.vavr.collection.Set;
import io.vavr.test.Arbitrary;
import io.vavr.test.Property;

@RunWith(MockitoJUnitRunner.class)
public class SetEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<Collection<String>, Collection<String>> delegate;

	@Mock
	private EndpointCall<Collection<String>> endpointCall;

	private SetEndpointCallHandlerAdapter<String, Collection<String>> adapter;

	private String expectedResult;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		adapter = new SetEndpointCallHandlerAdapter<>();

		expectedResult = "set result";

		when(endpointCall.execute())
			.thenReturn(Arrays.asList(expectedResult));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsSet() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("set"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotSet() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnCollectionWithArgumentTypeOfSet() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, String.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("set"))));
	}

	@Test
	public void shouldReturnCollectionWithObjectTypeWhenSetIsNotParameterized() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, Object.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbSet"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithSetReturnType() throws Exception {
		EndpointCallHandler<Set<String>, Collection<String>> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("set")), delegate);

		Set<String> set = handler.handle(endpointCall, null);

		assertNotNull(set);

		Property.def("contains endpoint call result on set")
				.forAll(Arbitrary.of(expectedResult))
				.suchThat(value -> set.contains(value))
					.check()
						.assertIsSatisfied();
	}

	interface SomeType {

		Set<String> set();

		@SuppressWarnings("rawtypes")
		Set dumbSet();

		String string();
	}
}
