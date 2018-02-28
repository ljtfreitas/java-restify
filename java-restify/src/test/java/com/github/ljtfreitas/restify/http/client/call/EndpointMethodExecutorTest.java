package com.github.ljtfreitas.restify.http.client.call;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutables;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestFactory;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

@RunWith(MockitoJUnitRunner.class)
public class EndpointMethodExecutorTest {

	@Mock
	private EndpointRequestFactory endpointRequestFactory;

	@Mock
	private EndpointCallExecutables endpointCallExecutables;

	@Mock
	private EndpointCallExecutable<Object, Object> executable;

	@Mock
	private EndpointCallFactory endpointCallFactory;

	@InjectMocks
	private EndpointMethodExecutor endpointMethodExecutor;

	private EndpointMethod endpointMethod;

	private EndpointRequest request;

	@Before
	public void setup() throws NoSuchMethodException, SecurityException {
		endpointMethod = new EndpointMethod(SomeType.class.getMethod("method"), "http://my.api.com/", "GET");

		when(endpointCallExecutables.of(endpointMethod))
			.thenReturn(executable);

		JavaType returnType = JavaType.of(String.class);

		when(executable.returnType())
			.thenReturn(returnType);

		request = new EndpointRequest(URI.create("http://my.api.com"), "GET");

		when(endpointRequestFactory.createWith(eq(endpointMethod), any(), eq(returnType)))
			.thenReturn(request);

		SimpleEndpointMethodCall call = new SimpleEndpointMethodCall("endpoint result");

		when(endpointCallFactory.createWith(request, returnType))
			.thenReturn(call);

		when(executable.execute(any(), any(Object[].class)))
			.then(i -> i.getArgumentAt(0, EndpointCall.class).execute());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldExecuteEndpointMethod() throws Exception {
		Object[] args = new Object[]{"arg"};

		Object result = endpointMethodExecutor.execute(endpointMethod, args);

		assertEquals("endpoint result", result);

		verify(endpointCallExecutables).of(endpointMethod);
		verify(endpointCallFactory).createWith(request, endpointMethod.returnType());
		verify(executable).execute(notNull(EndpointCall.class), eq(args));
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
