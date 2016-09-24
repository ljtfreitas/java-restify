package com.restify.http.contract.metadata;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.contract.Get;
import com.restify.http.contract.Method;
import com.restify.http.contract.Path;
import com.restify.http.contract.metadata.EndpointMethod;
import com.restify.http.contract.metadata.EndpointMethodReader;
import com.restify.http.contract.metadata.EndpointTarget;

public class EndpoinMethodReaderTest {

	private Class<RestType> javaType;

	private java.lang.reflect.Method javaGetMethod;
	private java.lang.reflect.Method javaPostMethod;

	@Before
	public void setup() throws Exception {
		javaType = RestType.class;

		javaGetMethod = javaType.getMethod("get");
		javaPostMethod = javaType.getMethod("post");
	}

	@Test
	public void shouldReadEndpointMethodsWithoutEndpointOnTarget() {
		EndpointTarget target = new EndpointTarget(javaType, null);

		EndpointMethod endpointMethod = new EndpointMethodReader(target).read(javaPostMethod);

		assertEquals("/context/post-method", endpointMethod.path());
		assertEquals("POST", endpointMethod.httpMethod());
	}

	@Test
	public void shouldReadEndpointMethodsWithEndpointOnTarget() throws Exception {
		EndpointTarget target = new EndpointTarget(javaType, "http://www.my.api.com");

		EndpointMethod endpointMethod = new EndpointMethodReader(target).read(javaGetMethod);

		assertEquals("http://www.my.api.com/context/get-method", endpointMethod.path());
		assertEquals("GET", endpointMethod.httpMethod());
	}

	@Path("/context")
	private interface RestType {

		@Path("/post-method") @Method("POST")
		void post();

		@Path("/get-method") @Get
		void get();

	}
}
