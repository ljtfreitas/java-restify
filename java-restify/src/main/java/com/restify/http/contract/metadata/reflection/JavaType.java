package com.restify.http.contract.metadata.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
}
