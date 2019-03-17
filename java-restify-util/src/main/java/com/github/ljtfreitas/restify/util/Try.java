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

public interface Try<T> {

	public static <T> Try<T> of(TryableSupplier<? extends T> supplier) {
		try {
			return success(supplier.get());
		} catch (Exception e) {
			return failure(e);
		}
	}

	public static Try<Void> run(TryableExpression expression) {
		try {
			expression.run();
			return new Success<>(null);
		} catch (Exception e) {
			return new Failure<>(e);
		}
	}

	public static <T extends AutoCloseable> TryWithResources<T> withResources(TryableSupplier<? extends T> supplier) {
		return new TryWithResources<>(supplier);
	}

	public static <T> Try<T> success(T value) {
		return new Success<>(value);
	}

	public static <T> Try<T> failure(Throwable throwable) {
		return new Failure<>(throwable);
	}

	public static void silently(TryableExpression... expressions) {
		for (TryableExpression expression : expressions) {
			try {
				expression.run();
			} catch (Exception e) {
			}
		}
	}

	public <U> Try<U> map(TryableFunction<T, ? extends U> mapper);

	public <U> Try<U> flatMap(Function<T, Try<? extends U>> mapper);

	public Try<T> apply(TryableConsumer<T> consumes);
	

	public Try<T> error(Function<? super Throwable, ? extends Throwable> mapper);

	public Try<T> recover(Function<? super Throwable, Try<? extends T>> mapper);

	public <E extends Throwable> Try<T> recover(Class<E> type, Function<E, Try<? extends T>> mapper);

	public T or(Supplier<T> mapper);

	public T get();

	public void apply();

	public class Success<T> implements Try<T> {

		private final T value;

		private Success(T value) {
			this.value = value;
		}

		@Override
		public <U> Try<U> map(TryableFunction<T, ? extends U> mapper) {
			try {
				return new Success<>(mapper.apply(value));
			} catch (Exception e) {
				return new Failure<>(e);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> Try<U> flatMap(Function<T, Try<? extends U>> mapper) {
			return (Try<U>) mapper.apply(value);
		}

		@Override
		public Try<T> apply(TryableConsumer<T> consumes) {
			try {
				consumes.accept(value);
				return this;
			} catch (Exception e) {
				return new Failure<>(e);
			}
		}

		@Override
		public Try<T> error(Function<? super Throwable, ? extends Throwable> mapper) {
			return this;
		}

		@Override
		public Try<T> recover(Function<? super Throwable, Try<? extends T>> mapper) {
			return this;
		}
		
		@Override
		public <E extends Throwable> Try<T> recover(Class<E> type, Function<E, Try<? extends T>> mapper) {
			return this;
		}

		@Override
		public T or(Supplier<T> mapper) {
			return value;
		}

		@Override
		public T get() {
			return value;
		}
		
		@Override
		public void apply() {
		}
	}

	public class Failure<T> implements Try<T> {

		private final Throwable cause;

		private Failure(Throwable cause) {
			this.cause = cause;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> Try<U> map(TryableFunction<T, ? extends U> mapper) {
			return (Try<U>) this;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> Try<U> flatMap(Function<T, Try<? extends U>> mapper) {
			return (Try<U>) this;
		}

		@Override
		public Try<T> apply(TryableConsumer<T> consumes) {
			return this;
		}

		@Override
		public Try<T> error(Function<? super Throwable, ? extends Throwable> mapper) {
			return new Failure<>(mapper.apply(cause));
		}

		@SuppressWarnings("unchecked")
		@Override
		public Try<T> recover(Function<? super Throwable, Try<? extends T>> mapper) {
			return (Try<T>) mapper.apply(cause);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <E extends Throwable> Try<T> recover(Class<E> type, Function<E, Try<? extends T>> mapper) {
			if (type.isAssignableFrom(cause.getClass())) {
				return (Try<T>) mapper.apply((E) cause);
			} else {
				return this;
			}
		}

		@Override
		public T or(Supplier<T> mapper) {
			return mapper.get();
		}

		@Override
		public T get() {
			throw (cause instanceof RuntimeException) ? (RuntimeException) cause : new RuntimeException(cause);
		}
		
		@Override
		public void apply() {
			throw (cause instanceof RuntimeException) ? (RuntimeException) cause : new RuntimeException(cause);
		}
	}

	public class TryWithResources<T extends AutoCloseable> {

		private final TryableSupplier<? extends T> supplier;

		public TryWithResources(TryableSupplier<? extends T> supplier) {
			this.supplier = supplier;
		}

		public Try<T> apply(TryableConsumer<? super T> consumer) {
			return Try.of(() -> {
				try (T closeable = supplier.get()) {
					consumer.accept(closeable);
					return closeable;
				}
			});
		}

		public <U> Try<U> map(TryableFunction<T, ? extends U> mapper) {
			return Try.of(() -> {
				try (T closeable = supplier.get()) {
					return mapper.apply(closeable);
				}
			});
		}
	}

	public interface TryableSupplier<T> {
		T get() throws Exception;
	}

	public interface TrowableSupplier<T> {
		T get() throws Throwable;
	}

	public interface TryableConsumer<T> {
		void accept(T t) throws Exception;
	}

	public interface TryableFunction<T, R> {
		R apply(T t) throws Exception;
	}

	public interface TryableExpression {
		void run() throws Exception;
	}
}
