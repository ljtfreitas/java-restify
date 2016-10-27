package com.restify.http.client.call;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.restify.http.client.Headers;
import com.restify.http.client.request.EndpointRequest;
import com.restify.http.client.request.EndpointRequestExecutor;
import com.restify.http.client.request.EndpointRequestFactory;
import com.restify.http.client.response.EndpointResponse;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.reflection.JavaType;
import com.restify.http.contract.metadata.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class EndpointCallFactoryTest {

	@Mock
	private EndpointRequestFactory endpointRequestFactoryMock;

	@Mock
	private EndpointRequestExecutor endpointRequestExecutorMock;

	@InjectMocks
	private EndpointCallFactory factory;

	@Mock
	private EndpointMethod endpointMethod;

	@Mock
	private EndpointRequest endpointRequest;

	private Object[] args;

	@Before
	public void setup() throws Exception {
		when(endpointRequestFactoryMock.createWith(same(endpointMethod), any(), any()))
			.thenReturn(endpointRequest);

		args = new Object[0];
	}

	@Test
	public void shouldCreateDefaultEndpointCall() {
		JavaType returnType = JavaType.of(String.class);

		EndpointCall<Object> call = factory.createWith(endpointMethod, args, returnType);

		assertNotNull(call);
		assertTrue(call instanceof DefaultEndpointCall);

		verify(endpointRequestFactoryMock).createWith(endpointMethod, args, returnType);
	}

	@Test
	public void shouldExtractRawResponseTypeWhenReturnTypeIsParameterizedEndpointResponse() {
		SimpleParameterizedType endpointResponseType = new SimpleParameterizedType(EndpointResponse.class, null, String.class);

		JavaType returnType = JavaType.of(endpointResponseType);

		EndpointCall<Object> call = factory.createWith(endpointMethod, args, returnType);

		assertNotNull(call);
		assertTrue(call instanceof EndpointResponseCall);

		verify(endpointRequestFactoryMock).createWith(endpointMethod, args, JavaType.of(String.class));
	}

	@Test
	public void shouldCreateCallWithObjectClassTypeWhenReturnTypeIsNotParameterizedEndpointResponse() {
		JavaType returnType = JavaType.of(EndpointResponse.class);

		EndpointCall<Object> call = factory.createWith(endpointMethod, args, returnType);

		assertNotNull(call);
		assertTrue(call instanceof EndpointResponseCall);

		verify(endpointRequestFactoryMock).createWith(endpointMethod, args, JavaType.of(Object.class));
	}

	@Test
	public void shouldCreateEndpointHeadersCallWhenReturnTypeIsHeaders() {
		JavaType returnType = JavaType.of(Headers.class);

		EndpointCall<Object> call = factory.createWith(endpointMethod, args, returnType);

		assertNotNull(call);
		assertTrue(EndpointHeadersCall.class.isInstance(call));

		verify(endpointRequestFactoryMock).createWith(endpointMethod, args, returnType);
	}

	@Test
	public void shouldCreateEndpointCallDecoratorWhenReturnTypeIsEndpointCall() {
		JavaType returnType = JavaType.of(new SimpleParameterizedType(EndpointCall.class, null, String.class));

		when(endpointRequest.responseType())
			.thenReturn(JavaType.of(String.class));

		EndpointCall<Object> call = factory.createWith(endpointMethod, args, returnType);

		assertNotNull(call);
		assertTrue(EndpointCallDecorator.class.isInstance(call));

		verify(endpointRequestFactoryMock).createWith(endpointMethod, args, JavaType.of(String.class));
	}

	interface SomeType {
		String method();
	}
}
