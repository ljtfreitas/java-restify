package com.restify.http.spring.client.call.exec;

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

import com.restify.http.client.call.exec.EndpointCallExecutable;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class HttpHeadersEndpointCallExecutableFactoryTest {

	@Mock
	private Converter<EndpointResponse<Object>, ResponseEntity<Object>> endpointResponseConverterMock;

	@Mock
	private EndpointResponse<Object> endpointResponseMock;

	@InjectMocks
	private HttpHeadersEndpointCallExecutableFactory factory;

	private HttpHeaders httpHeaders;

	@Before
	public void setup() {
		httpHeaders = new HttpHeaders();

		when(endpointResponseConverterMock.convert(endpointResponseMock))
			.thenReturn(new ResponseEntity<>(httpHeaders, HttpStatus.OK));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsHttpHeaders() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("httpHeaders"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsHttpHeaders() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldCreateExecutableWithParameterizedEndpointResponseReturnTypeWhenEndpointMethodReturnTypeIsHttpHeaders() throws Exception {
		EndpointCallExecutable<HttpHeaders, EndpointResponse<Object>> executable = factory.create(new SimpleEndpointMethod(SomeType.class.getMethod("httpHeaders")));

		HttpHeaders result = executable.execute(() -> endpointResponseMock);

		assertEquals(httpHeaders, result);

		assertEquals(JavaType.of(new SimpleParameterizedType(EndpointResponse.class, null, Object.class)), executable.returnType());
	}

	interface SomeType {

		HttpHeaders httpHeaders();

		String string();
	}

	private class SimpleEndpointMethod extends EndpointMethod {
		public SimpleEndpointMethod(Method javaMethod) {
			super(javaMethod, "/", "GET");
		}
	}
}
