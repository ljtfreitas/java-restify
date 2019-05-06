package com.github.ljtfreitas.restify.http.call.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class CollectionEndpointCallHandlerFactoryTest {

	@Mock
	private EndpointCall<Collection<String>> endpointCall;

	private CollectionEndpointCallHandlerFactory<String> factory;

	@Before
	public void setup() {
		factory = new CollectionEndpointCallHandlerFactory<>();
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsCollection() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("collection"))));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsList() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("list"))));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsSet() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("set"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeIsNotCollection() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithCollectionReturnType() throws Exception {
		EndpointCallHandler<Collection<String>, Collection<String>> handler = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("collection")));

		Collection<String> result = Arrays.asList("result");

		when(endpointCall.execute())
			.thenReturn(result);

		Collection<String> collection = handler.handle(endpointCall, null);

		assertSame(result, collection);
		assertEquals(JavaType.parameterizedType(Collection.class, null, String.class), handler.returnType());
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithSetReturnType() throws Exception {
		EndpointCallHandler<Collection<String>, Collection<String>> handler = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("set")));

		Set<String> result = Collections.singleton("result");

		when(endpointCall.execute())
			.thenReturn(result);

		Collection<String> collection = handler.handle(endpointCall, null);

		assertSame(result, collection);
		assertEquals(JavaType.parameterizedType(Set.class, null, String.class), handler.returnType());
	}

	@Test
	public void shouldCreateHandlerFromEndpointMethodWithListReturnType() throws Exception {
		EndpointCallHandler<Collection<String>, Collection<String>> handler = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("list")));

		Collection<String> result = Arrays.asList("result");

		when(endpointCall.execute())
			.thenReturn(result);

		Collection<String> collection = handler.handle(endpointCall, null);

		assertSame(result, collection);
		assertEquals(JavaType.parameterizedType(List.class, null, String.class), handler.returnType());
	}

	interface SomeType {

		Collection<String> collection();

		List<String> list();

		Set<String> set();

		String string();
	}
}
