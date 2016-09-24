package com.restify.http.contract.metadata.reflection;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

public class SimpleGenericArrayType implements GenericArrayType {

	private final Type componentType;

	public SimpleGenericArrayType(Type componentType) {
		this.componentType = componentType;
	}

	@Override
	public Type getGenericComponentType() {
		return componentType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(componentType);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GenericArrayType) {
			return Objects.equals(componentType, ((GenericArrayType) obj).getGenericComponentType());

		} else
			return super.equals(obj);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report.append("SimpleGenericArrayType: [")
			.append("Component Type: ")
				.append(componentType)
			.append("]");

		return report.toString();
	}

}
