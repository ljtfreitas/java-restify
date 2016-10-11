package com.restify.http.util;

import java.util.function.Supplier;

public interface Tryable {

	public static <T> T of(TryableSupplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <X extends Throwable, T> T of(TryableSupplier<T> supplier, Supplier<? extends X> exception) throws X {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw exception.get();
		}
	}

	public interface TryableSupplier<T> {
		T get() throws Exception;
	}
}
