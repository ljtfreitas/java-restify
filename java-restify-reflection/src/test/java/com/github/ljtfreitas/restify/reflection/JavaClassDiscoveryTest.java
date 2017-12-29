package com.github.ljtfreitas.restify.reflection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JavaClassDiscoveryTest {

	@Test
	public void shouldReturnTrueWhenClassNameExistsOnClasspath() {
		assertTrue(JavaClassDiscovery.present("org.junit.Assert"));
	}

	@Test
	public void shouldReturnFalseWhenClassNameNotExistsOnClasspath() {
		assertFalse(JavaClassDiscovery.present("com.whatever.Whatever"));
	}
}
