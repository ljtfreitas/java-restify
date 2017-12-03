package com.github.ljtfreitas.restify.reflection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;

import org.junit.Test;

public class JavaTypeResolverTest {

	@Test
	public void shouldResolveSimpleTypeVariableMethodReturnTypeByClassContext() throws Exception {
		JavaTypeResolver resolver = new JavaTypeResolver(MyTyped.class);

		Type returnType = resolver.returnTypeOf(MyTyped.class.getMethod("method"));

		assertEquals(String.class, returnType);
	}

	@Test
	public void shouldResolveParameterizedTypeVariableMethodReturnTypeByClassContext() throws Exception {
		JavaTypeResolver resolver = new JavaTypeResolver(MyTyped.class);

		Type returnType = resolver.returnTypeOf(MyTyped.class.getMethod("method2"));

		assertEquals(JavaType.parameterizedType(Collection.class, String.class), returnType);
	}

	@Test
	public void shouldResolveArrayMethodReturnTypeByClassContext() throws Exception {
		JavaTypeResolver resolver = new JavaTypeResolver(MyTyped.class);

		Type returnType = resolver.returnTypeOf(MyTyped.class.getMethod("method3"));

		assertEquals(String[].class, returnType);
	}

	@Test
	public void shouldResolveGenericArrayMethodReturnTypeByClassContext() throws Exception {
		JavaTypeResolver resolver = new JavaTypeResolver(MyTyped.class);

		Type returnType = resolver.returnTypeOf(MyTyped.class.getMethod("method4"));

		assertEquals(JavaType.arrayType(String.class), returnType);
	}

	@Test
	public void shouldResolveTypeVariableMethodReturnTypeByClassContext() throws Exception {
		JavaTypeResolver resolver = new JavaTypeResolver(MyTyped.class);

		Method method = MyTyped.class.getMethod("method5", Typed.class);

		Type returnType = resolver.returnTypeOf(method);

		assertThat(returnType, instanceOf(TypeVariable.class));
	}

	@Test
	public void shouldResolveWildcardParameterTypeByClassContext() throws Exception {
		JavaTypeResolver resolver = new JavaTypeResolver(MyTyped.class);

		Method method = MyTyped.class.getMethod("method5", Typed.class);

		Type parameterType = resolver.parameterizedTypeOf(method.getParameters()[0]);

		assertThat(parameterType, instanceOf(ParameterizedType.class));

		ParameterizedType parameterizedType = (ParameterizedType) parameterType;

		assertThat(parameterizedType.getActualTypeArguments(), arrayWithSize(1));

		Type argumentType = parameterizedType.getActualTypeArguments()[0];

		assertThat(argumentType, instanceOf(WildcardType.class));

		WildcardType wildcardType = (WildcardType) argumentType;

		assertThat(wildcardType.getUpperBounds(), arrayWithSize(1));
		assertThat(wildcardType.getUpperBounds()[0], equalTo(Object.class));

		assertThat(wildcardType.getLowerBounds(), arrayWithSize(1));
		assertThat(wildcardType.getLowerBounds()[0], equalTo(String.class));
	}

	@Test
	public void shouldGetRawClassOfClassType() {
		assertEquals(String.class, JavaTypeResolver.rawClassTypeOf(String.class));
	}

	@Test
	public void shouldGetRawClassOfParameterizedType() {
		ParameterizedType parameterizedType = JavaType.parameterizedType(Collection.class, String.class);

		assertEquals(Collection.class, JavaTypeResolver.rawClassTypeOf(parameterizedType));
	}

	@Test
	public void shouldGetRawClassOfArrayType() {
		GenericArrayType arrayType = JavaType.arrayType(String.class);

		assertEquals(String[].class, JavaTypeResolver.rawClassTypeOf(arrayType));
	}

	@Test
	public void shouldGetRawClassOfWildcardType() {
		WildcardType wildcardType = JavaType.wildcardType(new Type[] { Object.class }, new Type[] { String.class });

		assertEquals(Object.class, JavaTypeResolver.rawClassTypeOf(wildcardType));
	}

	interface Typed<T> {

		T method();

		Collection<T> method2();

		String[] method3();

		T[] method4();

		<E extends T> E method5(Typed<? super T> parameter);
	}

	interface MyTyped extends Typed<String> {
	}
}
