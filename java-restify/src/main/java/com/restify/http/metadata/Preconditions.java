package com.restify.http.metadata;

import java.util.Objects;

public class Preconditions {

	public static <T> T nonNull(T value, String message) {
		return Objects.requireNonNull(value, message);
	}

	public static boolean isTrue(boolean value, String message) {
		if (!value) throw new IllegalArgumentException(message);
		return value;
	}

	public static boolean isFalse(boolean value, String message) {
		if (value) throw new IllegalArgumentException(message);
		return value;
	}

}
