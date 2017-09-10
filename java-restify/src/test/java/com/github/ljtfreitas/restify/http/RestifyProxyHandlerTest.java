package com.github.ljtfreitas.restify.http;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.ljtfreitas.restify.http.client.call.EndpointMethodExecutor;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethods;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointTarget;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointType;

@RunWith(MockitoJUnitRunner.class)
public class RestifyProxyHandlerTest {

	@Mock
	private EndpointMethodExecutor endpointMethodExecutorMock;

	private RestifyProxyHandler restifyProxyHandler;

	private TargetType targetType;

	private EndpointType endpointType;

	private EndpointMethod endpointMethod;

	@Before
	public void setup() throws Exception {
		EndpointTarget target = new EndpointTarget(TargetType.class, "http://my.api.com");

		endpointMethod = new EndpointMethod(TargetType.class.getMethod("method"), "/", "GET");

		endpointType = Mockito.spy(new EndpointType(target, new EndpointMethods(Arrays.asList(endpointMethod))));

		when(endpointMethodExecutorMock.execute(same(endpointMethod), any()))
			.thenReturn("Result");

		restifyProxyHandler = new RestifyProxyHandler(endpointType, endpointMethodExecutorMock);

		targetType = (TargetType) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{TargetType.class}, restifyProxyHandler);
	}

	@Test
	public void shouldInvokeEndpointMethod() {
		String result = targetType.method();

		assertEquals("Result", result);

		verify(endpointMethodExecutorMock)
			.execute(same(endpointMethod), any());
	}

	@Test
	public void shouldInvokeToStringMethodOnEndpointType() {
		String targetTypeToString = targetType.toString();

		assertEquals(endpointType.toString(), targetTypeToString);
	}

	@Test
	public void shouldInvokeHashCodeMethodOnEndpointType() {
		int targetTypeHashCode = targetType.hashCode();

		assertEquals(endpointType.hashCode(), targetTypeHashCode);
	}

	@Test
	public void shouldInvokeEqualsMethodOnEndpointType() {
		boolean targetTypeEquals = targetType.equals(targetType);

		assertEquals(endpointType.equals(endpointType), targetTypeEquals);
	}

	@Test
	public void shouldInvokeDefaultMethodOnInterfaceType() {
		assertEquals("TargetType default method", targetType.myDefaultMethod());
	}

	@Test
	public void shouldInvokeStaticMethodOnInterfaceType() {
		assertEquals("TargetType default static method", TargetType.myStaticMethod());
	}

	public interface TargetType {

		String method();

		default String myDefaultMethod() {
			return "TargetType default method";
		}

		static String myStaticMethod() {
			return "TargetType default static method";
		}
	}
}
