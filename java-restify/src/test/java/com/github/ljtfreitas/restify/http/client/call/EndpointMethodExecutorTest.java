package com.github.ljtfreitas.restify.http.client.call;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutables;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class EndpointMethodExecutorTest {

	@Mock
	private EndpointCallExecutables endpointCallExecutablesMock;

	@Mock
	private EndpointCallExecutable<Object, Object> endpointCallExecutableMock;

	@Mock
	private EndpointCallFactory endpointCallFactoryMock;

	@InjectMocks
	private EndpointMethodExecutor endpointMethodExecutor;

	private EndpointMethod endpointMethod;

	@Before
	public void setup() throws NoSuchMethodException, SecurityException {
		endpointMethod = new EndpointMethod(SomeType.class.getMethod("method"), "http://my.api.com/", "GET");

		when(endpointCallExecutablesMock.of(endpointMethod))
			.thenReturn(endpointCallExecutableMock);

		SimpleEndpointMethodCall call = new SimpleEndpointMethodCall("endpoint result");

		when(endpointCallFactoryMock.createWith(notNull(EndpointMethod.class), any(), notNull(JavaType.class)))
			.thenReturn(call);

		when(endpointCallExecutableMock.execute(any(), any(Object[].class)))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldExecuteEndpointMethod() throws Exception {
		Object[] args = new Object[]{"arg"};

		JavaType returnType = JavaType.of(String.class);

		when(endpointCallExecutableMock.returnType())
			.thenReturn(returnType);

		Object result = endpointMethodExecutor.execute(endpointMethod, args);

		assertEquals("endpoint result", result);

		verify(endpointCallExecutablesMock).of(endpointMethod);
		verify(endpointCallFactoryMock).createWith(notNull(EndpointMethod.class), any(), notNull(JavaType.class));
		verify(endpointCallExecutableMock).execute(notNull(EndpointCall.class), eq(args));
	}

	interface SomeType {
		String method();
	}

	private class SimpleEndpointMethodCall implements EndpointCall<Object> {

		private final Object result;

		private SimpleEndpointMethodCall(Object result) {
			this.result = result;
		}

		@Override
		public Object execute() {
			return result;
		}
	}
}
