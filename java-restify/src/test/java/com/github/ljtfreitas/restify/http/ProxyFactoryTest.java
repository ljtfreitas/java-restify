package com.github.ljtfreitas.restify.http;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Before;
import org.junit.Test;

public class ProxyFactoryTest {

	private TestInvocationHandler handler;

	@Before
	public void setup() {
		handler = new TestInvocationHandler();
	}

	@Test
	public void shouldCreateProxyUsingClassLoaderFromProxyfiedType() {
		ProxyFactory proxyFactory = new ProxyFactory(handler);

		WhateverType proxy = proxyFactory.create(WhateverType.class);

		assertEquals("result", proxy.whatever());
	}

	@Test
	public void shouldCreateProxyUsingCustomClassLoader() throws ClassNotFoundException {
		URL url = this.getClass().getClassLoader()
				.getResource("com/github/ljtfreitas/restify/http/WhateverType.class");

		ClassLoader customClassLoader = new URLClassLoader(new URL[] { url });

		ProxyFactory proxyFactory = new ProxyFactory(handler, customClassLoader);

		WhateverType proxy = proxyFactory.create(WhateverType.class);

		assertEquals("result", proxy.whatever());
	}

	private class TestInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return "result";
		}
	}
}
