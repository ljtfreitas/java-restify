package com.restify.http.client.request;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import com.restify.http.client.response.EndpointResponse;

public class ExpectedType {

	private final Type expectedType;
	private final Type rawExpectedType;

	private ExpectedType(Type expectedType, Class<?> disposableClassType) {
		this.expectedType = expectedType;
		this.rawExpectedType = rawExpectedTypeOf(expectedType, disposableClassType);
	}

	private Type rawExpectedTypeOf(Type expectedType, Class<?> disposableClassType) {
		if (expectedType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) expectedType;
			Type parameterizedRawType = parameterizedType.getRawType();

			if (isTypeOf(parameterizedRawType, disposableClassType)) {
				return parameterizedType.getActualTypeArguments()[0];
			} else {
				return parameterizedType;
			}
		} else {
			return expectedType;
		}
	}

	private boolean isTypeOf(Type type, Class<?> expectedClassType) {
		if (type instanceof Class) {
			return expectedClassType.isAssignableFrom((Class<?>) type);

		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type rawType = parameterizedType.getRawType();

			if (rawType instanceof Class) {
				return expectedClassType.isAssignableFrom((Class<?>) rawType);

			} else {
				return rawType.equals(expectedClassType);
			}

		} else {
			return false;
		}
	}

	public Type type() {
		return rawExpectedType;
	}

	public boolean voidType() {
		return expectedType == Void.TYPE || expectedType == Void.class;
	}

	public boolean is(Class<?> expectedClassType) {
		return isTypeOf(expectedType, expectedClassType);
	}

	public ExpectedType dispose(Class<?> disposableClassType) {
		return ExpectedType.of(expectedType, disposableClassType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(expectedType, rawExpectedType);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ExpectedType) {
			ExpectedType that = (ExpectedType) obj;

			return Objects.equals(expectedType, that.expectedType)
					&& Objects.equals(rawExpectedType, that.rawExpectedType);

		} else
			return false;
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report.append("EndpointExpectedType: [")
					.append("Expected Type: ")
						.append(expectedType)
					.append(", ")
					.append("Raw Expected Type: ")
						.append(rawExpectedType)
				.append("]");

		return report.toString();
	}

	public static ExpectedType of(Type expectedType) {
		return new ExpectedType(expectedType, EndpointResponse.class);
	}

	public static ExpectedType of(Type expectedType, Class<?> unwrapClassType) {
		return new ExpectedType(expectedType, unwrapClassType);
	}
}
