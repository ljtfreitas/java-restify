package com.github.ljtfreitas.restify.reflection;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

public class JavaTypeTest {

	@Test
	public void shouldGetRawClassTypeOfParameterizedType() {
		ParameterizedType parameterizedType = JavaType.parameterizedType(Collection.class, String.class);

		JavaType type = JavaType.of(parameterizedType);

		assertEquals(Collection.class, type.classType());
	}

	@Test
	public void shouldGetRawClassTypeOfGenericArrayType() {
		GenericArrayType arrayType = JavaType.arrayType(String.class);

		JavaType type = JavaType.of(arrayType);

		assertEquals(String[].class, type.classType());
	}

	@Test
	public void shouldGetRawClassTypeOfWildcardType() throws Exception {
		WildcardType wildcardType = JavaType.wildcardType(new Type[] { Number.class }, null);

		JavaType type = JavaType.of(wildcardType);

		assertEquals(Number.class, type.classType());
	}

	@Test
	public void shouldCheckWhenTypeIsVoid() throws Exception {
		JavaType type = JavaType.of(void.class);
		assertTrue(type.voidType());

		type = JavaType.of(Void.TYPE);
		assertTrue(type.voidType());

		type = JavaType.of(String.class);
		assertFalse(type.voidType());
	}

	@Test
	public void shouldCheckWhenTypeIsArray() throws Exception {
		JavaType type = JavaType.of(String[].class);
		assertTrue(type.arrayType());

		type = JavaType.of(String.class);
		assertFalse(type.arrayType());
	}

	@Test
	public void shouldCheckTypeByClassType() throws Exception {
		JavaType type = JavaType.of(String.class);

		assertTrue(type.is(String.class));
		assertFalse(type.is(Integer.class));
	}

	@Test
	public void shouldCheckTypeByParameterizedType() throws Exception {
		JavaType type = JavaType.of(JavaType.parameterizedType(Collection.class, String.class));

		assertTrue(type.is(Collection.class));
		assertFalse(type.is(Integer.class));
	}

	@Test
	public void shouldCheckAssignableFromType() throws Exception {
		JavaType type = JavaType.of(JavaType.parameterizedType(Collection.class, String.class));

		assertTrue(type.isAssignableFrom(List.class));
	}

	@Test
	public void shouldCheckWhenTypeIsParameterized() throws Exception {
		JavaType type = JavaType.of(JavaType.parameterizedType(Collection.class, String.class));
		assertTrue(type.parameterized());

		type = JavaType.of(String.class);
		assertFalse(type.parameterized());
	}

	@Test
	public void shouldCastJavaTypeToLowLevelType() throws Exception {
		JavaType type = JavaType.of(String.class);

		Class<?> classType = type.as(Class.class);

		assertNotNull(classType);
	}

	@Test(expected = ClassCastException.class)
	public void shouldThrowExceptionWhenTryCastJavaTypeToIncompatibleLowLevelType() throws Exception {
		JavaType type = JavaType.of(String.class);

		type.as(ParameterizedType.class);
	}

	@Test
	public void shouldUnwrapJavaTypeToLowLevelType() throws Exception {
		JavaType type = JavaType.of(JavaType.parameterizedType(Collection.class, String.class));

		Type lowLevelType = type.unwrap();

		assertThat(lowLevelType, instanceOf(ParameterizedType.class));

		ParameterizedType parameterizedType = (ParameterizedType) lowLevelType;

		assertEquals(parameterizedType.getRawType(), Collection.class);
		assertEquals(parameterizedType.getActualTypeArguments()[0], String.class);
	}

	@Test
	public void shouldBeEqualsWhenOtherJavaTypeIsSameType() throws Exception {
		JavaType type = JavaType.of(String.class);

		JavaType that = JavaType.of(String.class);

		assertEquals(type, that);
	}

	@Test
	public void shouldNotBeEqualsWhenOtherJavaTypeIsNotASameType() throws Exception {
		JavaType type = JavaType.of(String.class);

		JavaType that = JavaType.of(Integer.class);

		assertNotEquals(type, that);
	}
}
