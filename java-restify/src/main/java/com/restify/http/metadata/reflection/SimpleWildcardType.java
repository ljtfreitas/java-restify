package com.restify.http.metadata.reflection;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;

public class SimpleWildcardType implements WildcardType {

	private final Type[] upperBounds;
	private final Type[] lowerBounds;
	
	public SimpleWildcardType(Type[] upperBounds, Type[] lowerBounds) {
		this.upperBounds = upperBounds;
		this.lowerBounds = lowerBounds;
	}

	@Override
	public Type[] getUpperBounds() {
		return upperBounds;
	}

	@Override
	public Type[] getLowerBounds() {
		return lowerBounds;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(upperBounds, lowerBounds);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WildcardType) {
			WildcardType that = (WildcardType) obj;
			
			return Arrays.equals(upperBounds, that.getUpperBounds())
				&& Arrays.equals(lowerBounds, that.getLowerBounds());
					
		} else return super.equals(obj);
	}

}
