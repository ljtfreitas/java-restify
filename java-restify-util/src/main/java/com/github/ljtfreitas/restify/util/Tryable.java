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
package com.github.ljtfreitas.restify.util;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Tryable {

	public static <T> T of(TryableSupplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T or(TryableSupplier<T> supplier, T onError) {
		try {
			return supplier.get();
		} catch (Exception e) {
			return onError;
		}
	}

	public static <X extends Throwable, T> T of(TryableSupplier<T> supplier, Supplier<? extends X> exception) throws X {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw exception.get();
		}
	}

	public static <X extends Throwable, T> T of(TryableSupplier<T> supplier, Function<? super Exception, ? extends X> exception) throws X {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw exception.apply(e);
		}
	}

	public static void run(TryableExpression expression) {
		try {
			expression.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void silently(TryableExpression expression) {
		try {
			expression.run();
		} catch (Exception e) {
		}
	}

	public interface TryableSupplier<T> {
		T get() throws Exception;
	}

	public interface TryableExpression {
		void run() throws Exception;
	}
}
