package com.github.ljtfreitas.restify.http.client.call;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.request.EndpointRequest;
import com.github.ljtfreitas.restify.http.client.request.EndpointRequestExecutor;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEndpointCallFactoryTest {

	@Mock
	private EndpointRequestExecutor endpointRequestExecutor;

	@InjectMocks
	private DefaultEndpointCallFactory factory;

	@Mock
	private EndpointMethod endpointMethod;

	private EndpointRequest endpointRequest;

	@Before
	public void setup() throws Exception {
		endpointRequest = new EndpointRequest(URI.create("http://my.api.com"), "GET");
	}

	@Test
	public void shouldCreateDefaultEndpointCall() {
		JavaType returnType = JavaType.of(String.class);

		EndpointCall<Object> call = factory.createWith(endpointRequest, returnType);

		assertNotNull(call);
		assertTrue(call instanceof DefaultEndpointCall);
	}

	@Test
	public void shouldExtractRawResponseTypeWhenReturnTypeIsParameterizedEndpointResponse() {
		SimpleParameterizedType endpointResponseType = new SimpleParameterizedType(EndpointResponse.class, null, String.class);

		JavaType returnType = JavaType.of(endpointResponseType);

		EndpointCall<Object> call = factory.createWith(endpointRequest, returnType);

		assertNotNull(call);
		assertTrue(call instanceof EndpointResponseCall);
	}

	@Test
	public void shouldCreateCallWithObjectClassTypeWhenReturnTypeIsNotParameterizedEndpointResponse() {
		JavaType returnType = JavaType.of(EndpointResponse.class);

		EndpointCall<Object> call = factory.createWith(endpointRequest, returnType);

		assertNotNull(call);
		assertTrue(call instanceof EndpointResponseCall);
	}

}
