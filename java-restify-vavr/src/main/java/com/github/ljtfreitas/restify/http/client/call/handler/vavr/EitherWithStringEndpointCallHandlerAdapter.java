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
package com.github.ljtfreitas.restify.http.client.call.handler.vavr;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandler;
import com.github.ljtfreitas.restify.http.client.call.handler.EndpointCallHandlerAdapter;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;

import io.vavr.control.Either;
import io.vavr.control.Try;

public class EitherWithStringEndpointCallHandlerAdapter<T, O> implements EndpointCallHandlerAdapter<Either<String, T>, T, O> {

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(Either.class) && supportedLeftType(endpointMethod.returnType());
	}

	private boolean supportedLeftType(JavaType returnType) {
		if (!returnType.parameterized()) return false;

		ParameterizedType parameterizedType = returnType.as(ParameterizedType.class);

		if (parameterizedType.getActualTypeArguments().length == 0) return false;

		JavaType leftType = JavaType.of(parameterizedType.getActualTypeArguments()[0]);

		return String.class.isAssignableFrom(leftType.classType());
	}

	@Override
	public JavaType returnType(EndpointMethod endpointMethod) {
		return JavaType.of(rightType(endpointMethod.returnType()));
	}

	private Type rightType(JavaType declaredReturnType) {
		return declaredReturnType.parameterized() ?
				declaredReturnType.as(ParameterizedType.class).getActualTypeArguments()[1] :
					Object.class;
	}

	@Override
	public EndpointCallHandler<Either<String, T>, O> adapt(EndpointMethod endpointMethod, EndpointCallHandler<T, O> handler) {
		return new EitherWithStringEndpointMethodHandler(handler);
	}

	private class EitherWithStringEndpointMethodHandler implements EndpointCallHandler<Either<String, T>, O> {

		private final EndpointCallHandler<T, O> delegate;

		private EitherWithStringEndpointMethodHandler(EndpointCallHandler<T, O> delegate) {
			this.delegate = delegate;
		}

		@Override
		public JavaType returnType() {
			return delegate.returnType();
		}

		@Override
		public Either<String, T> handle(EndpointCall<O> call, Object[] args) {
			return Try.of(() -> delegate.handle(call, args))
					.fold(e -> left(e), r -> right(r));
		}

		private Either<String, T> right(T response) {
			return Either.right(response);
		}

		private Either<String, T> left(Throwable e) {
			return Either.left(e.getMessage());
		}
	}
}
