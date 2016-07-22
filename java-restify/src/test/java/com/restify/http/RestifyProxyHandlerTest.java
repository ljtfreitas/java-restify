package com.restify.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.restify.http.contract.Get;
import com.restify.http.contract.Path;

public class RestifyProxyHandlerTest {

	private TargetType targetType;

	@Before
	public void setup() throws Exception {
		targetType = new RestifyProxyBuilder()
			.target(TargetType.class)
				.build();
	}

	@Test
	public void shouldInvokeToStringMethodOnEndpointType() {
		assertTrue(targetType.toString().startsWith("EndpointType"));
	}

	@Test
	public void shouldInvokeDefaultMethodOnInterfaceType() {
		assertEquals("TargetType default method", targetType.test());
	}

	@Test
	public void shouldInvokeStaticMethodOnInterfaceType() {
		assertEquals("TargetType default static method", TargetType.staticTest());
	}

	@Test
	public void shouldInvokeVoidMethodOnEndpointType() {
		targetType.voidMethod();
	}

	@Path("http://www.google.com")
	public interface TargetType {

		@Path("/path") @Get
		String method();

		@Path("/") @Get
		void voidMethod();

		default String test() {
			return "TargetType default method";
		}

		static String staticTest() {
			return "TargetType default static method";
		}
	}
}
