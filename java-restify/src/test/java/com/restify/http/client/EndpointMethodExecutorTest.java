package com.restify.http.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.metadata.EndpointMethod;

@RunWith(MockitoJUnitRunner.class)
public class EndpointMethodExecutorTest {

	@Mock
	private EndpointRequestExecutor endpointRequestExecutorMock;

	@InjectMocks
	private EndpointMethodExecutor endpointMethodExecutor;

	@Test
	public void shouldExecuteEndpointMethod() throws Exception {
		EndpointMethod endpointMethod = new EndpointMethod(SomeType.class.getMethod("method"), "http://my.api.com/", "GET");

		when(endpointRequestExecutorMock.execute(notNull(EndpointRequest.class)))
			.thenReturn("endpoint result");

		Object[] args = new Object[]{"arg"};

		Object result = endpointMethodExecutor.execute(endpointMethod, args);

		assertEquals("endpoint result", result);
	}

	interface SomeType {

		String method();
	}
}
