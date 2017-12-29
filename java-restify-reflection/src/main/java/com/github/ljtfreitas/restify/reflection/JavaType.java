/*******************************************************************************
 *
 * MIT License
 *
 * Copyright (c) 2016 Tiago de Freitas Lima
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *******************************************************************************/
package com.github.ljtfreitas.restify.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public class JavaType {

	private final Type type;

	private JavaType(Type type) {
		this.type = type;
	}

	public static JavaType of(Type type) {
		return new JavaType(type);
	}

	public Class<?> classType() {
		return rawClassType();
	}

	private Class<?> rawClassType() {
		return JavaTypeResolver.rawClassTypeOf(type);
	}

	public boolean voidType() {
		Class<?> rawClassType = rawClassType();
		return rawClassType.equals(Void.class) || rawClassType.equals(Void.TYPE);
	}

	public boolean arrayType() {
		Class<?> rawClassType = rawClassType();
		return rawClassType.isArray();
	}

	public boolean is(Class<?> classType) {
		return rawClassType().equals(classType);
	}

	public boolean isAssignableFrom(Class<?> classType) {
		return rawClassType().isAssignableFrom(classType);
	}

	public boolean parameterized() {
		return type instanceof ParameterizedType;
	}

	public <T extends Type> T as(Class<T> javaType) {
		return javaType.cast(type);
	}

	public Type unwrap() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JavaType) {
			JavaType that = (JavaType) obj;
			return type.equals(that.type);

		} else return false;
	}

	@Override
	public String toString() {
		return "JavaType:[" + type + "]";
	}

	public static ParameterizedType parameterizedType(Class<?> rawType, Type... typeArguments) {
		return new SimpleParameterizedType(rawType, null, typeArguments);
	}

	public static GenericArrayType arrayType(Type componentType) {
		return new SimpleGenericArrayType(componentType);
	}

	public static WildcardType wildcardType(Type[] upperBounds, Type[] lowerBounds) {
		return new SimpleWildcardType(upperBounds, lowerBounds);
	}
}
