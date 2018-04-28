package com.github.ljtfreitas.restify.http.spring.client.call.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class HttpStatusEndpointCallExecutableFactoryTest {

	@Mock
	private EndpointCallExecutable<ResponseEntity<Void>, Void> delegate;

	@InjectMocks
	private HttpStatusEndpointCallExecutableFactory factory;

	private HttpStatus httpStatus;

	@Before
	public void setup() {
		httpStatus = HttpStatus.OK;

		ResponseEntity<Void> responseEntity = new ResponseEntity<>(null, new HttpHeaders(), httpStatus);

		when(delegate.execute(any(), anyVararg()))
			.thenReturn(responseEntity);

		when(delegate.returnType())
			.thenReturn(JavaType.of(Void.class));
	}

	@Test
	public void shouldSupportsWhenEndpointMethodReturnTypeIsHttpStatus() throws Exception {
		assertTrue(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("httpStatus"))));
	}

	@Test
	public void shouldNotSupportsWhenEndpointMethodReturnTypeNotIsHttpStatus() throws Exception {
		assertFalse(factory.supports(new SimpleEndpointMethod(SomeType.class.getMethod("string"))));
	}

	@Test
	public void shouldReturnResponseEntityDecoratedType() throws Exception {
		assertEquals(JavaType.of(new SimpleParameterizedType(ResponseEntity.class, null, Void.class)),
				factory.returnType(new SimpleEndpointMethod(SomeType.class.getMethod("httpStatus"))));
	}

	@Test
	public void shouldCreateExecutableWithResponseEntityReturnType() throws Exception {
		EndpointCallExecutable<HttpStatus, Void> executable = factory
				.create(new SimpleEndpointMethod(SomeType.class.getMethod("httpStatus")), delegate);

		HttpStatus result = executable.execute(() -> null, null);

		assertEquals(httpStatus, result);
	}

	interface SomeType {

		HttpStatus httpStatus();

		String string();
	}

	private class SimpleEndpointMethod extends EndpointMethod {
		public SimpleEndpointMethod(Method javaMethod) {
			super(javaMethod, "/", "GET");
		}
	}
}
