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
import com.github.ljtfreitas.restify.http.client.call.handler.vavr.ListEndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.vavr.collection.List;
import io.vavr.test.Arbitrary;
import io.vavr.test.Property;

@RunWith(MockitoJUnitRunner.class)
public class ListEndpointCallHandlerAdapterTest {

	@Mock
	private EndpointCallHandler<Collection<String>, Collection<String>> delegate;

	@Mock
	private EndpointCall<Collection<String>> endpointCall;

	private ListEndpointCallHandlerAdapter<String, Collection<String>> adapter;

	private String expectedResult;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		adapter = new ListEndpointCallHandlerAdapter<>();

		expectedResult = "list result";

		when(endpointCall.execute())
			.thenReturn(Arrays.asList(expectedResult));

		when(delegate.handle(notNull(EndpointCall.class), anyVararg()))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsList() throws Exception {
		assertTrue(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("list"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotList() throws Exception {
		assertFalse(adapter.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnCollectionWithArgumentTypeOfList() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, String.class),
				adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("list"))));
	}

	@Test
	public void shouldReturnCollectionWithObjectTypeWhenListIsNotParameterized() throws Exception {
		assertEquals(JavaType.parameterizedType(Collection.class, Object.class), adapter.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("dumbList"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithListReturnType() throws Exception {
		EndpointCallHandler<List<String>, Collection<String>> handler = adapter
				.adapt(new SimpleEndpointMethod(SomeType.class.getMethod("list")), delegate);

		List<String> list = handler.handle(endpointCall, null);

		assertNotNull(list);

		Property.def("contains endpoint call result on list")
				.forAll(Arbitrary.of(expectedResult))
				.suchThat(value -> list.contains(value))
					.check()
						.assertIsSatisfied();
	}

	interface SomeType {

		List<String> list();

		@SuppressWarnings("rawtypes")
		List dumbList();

		String string();
	}
}
