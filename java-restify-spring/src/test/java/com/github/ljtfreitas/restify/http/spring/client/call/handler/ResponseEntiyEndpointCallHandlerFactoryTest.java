package com.github.ljtfreitas.restify.http.spring.client.call.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class ResponseEntiyEndpointCallHandlerFactoryTest {

	@Mock
	private Converter<EndpointResponse<String>, ResponseEntity<String>> endpointResponseConverterMock;

	@Mock
	private EndpointResponse<String> endpointResponseMock;

	@InjectMocks
	private ResponseEntityEndpointCallHandlerFactory<String> factory;

	private ResponseEntity<String> responseEntity;

	@Before
	public void setup() {
		responseEntity = new ResponseEntity<>("expected result", new HttpHeaders(), HttpStatus.OK);

		when(endpointResponseConverterMock.convert(endpointResponseMock))
			.thenReturn(responseEntity);
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsResponseEntity() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("responseEntity"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsResponseEntity() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableWithParameterizedEndpointResponseReturnTypeWhenEndpointMethodReturnTypeIsResponseEntity() throws Exception {
		EndpointCallHandler<ResponseEntity<String>, EndpointResponse<String>> handler = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("responseEntity")));

		ResponseEntity<String> result = handler.handle(() -> endpointResponseMock, null);

		assertEquals(responseEntity, result);

		assertEquals(JavaType.of(new SimpleParameterizedType(EndpointResponse.class, null, String.class)), handler.returnType());
	}

	interface SomeType {

		ResponseEntity<String> responseEntity();

		String string();
	}

	private class SimpleEndpointMethod extends EndpointMethod {
		public SimpleEndpointMethod(Method javaMethod) {
			super(javaMethod, "/", "GET");
		}
	}
}
