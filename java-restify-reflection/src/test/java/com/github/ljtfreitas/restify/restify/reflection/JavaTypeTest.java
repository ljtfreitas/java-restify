package com.github.ljtfreitas.restify.restify.reflection;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.Collection;

import org.junit.Test;

import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleGenericArrayType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;
import com.github.ljtfreitas.restify.reflection.SimpleWildcardType;

public class JavaTypeTest {

	@Test
	public void shouldGetRawClassTypeOfParameterizedType() {
		JavaType type = JavaType.of(new SimpleParameterizedType(Collection.class, null, String.class));
		assertEquals(Collection.class, type.classType());
	}

	@Test
	public void shouldGetRawClassTypeOfGenericArrayType() {
		JavaType type = JavaType.of(new SimpleGenericArrayType(String.class));
		assertEquals(String[].class, type.classType());
	}

	@Test
	public void shouldGetRawClassTypeOfWildcardType() throws Exception {
		JavaType type = JavaType.of(new SimpleWildcardType(new Type[]{Number.class}, null));
		assertEquals(Number.class, type.classType());
	}
}
