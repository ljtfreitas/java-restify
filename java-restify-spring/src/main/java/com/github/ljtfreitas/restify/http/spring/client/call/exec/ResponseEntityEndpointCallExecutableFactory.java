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
package com.github.ljtfreitas.restify.http.spring.client.call.exec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.ResponseEntity;

import com.github.ljtfreitas.restify.http.client.call.EndpointCall;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutable;
import com.github.ljtfreitas.restify.http.client.call.exec.EndpointCallExecutableFactory;
import com.github.ljtfreitas.restify.http.client.response.EndpointResponse;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethod;
import com.github.ljtfreitas.restify.reflection.JavaType;
import com.github.ljtfreitas.restify.reflection.SimpleParameterizedType;

public class ResponseEntityEndpointCallExecutableFactory<T> implements EndpointCallExecutableFactory<ResponseEntity<T>, EndpointResponse<T>> {

	private final Converter<EndpointResponse<T>, ResponseEntity<T>> endpointResponseConverter;

	public ResponseEntityEndpointCallExecutableFactory() {
		this(new ResponseEntityConverter<>());
	}

	public ResponseEntityEndpointCallExecutableFactory(Converter<EndpointResponse<T>, ResponseEntity<T>> endpointResponseConverter) {
		this.endpointResponseConverter = endpointResponseConverter;
	}

	@Override
	public boolean supports(EndpointMethod endpointMethod) {
		return endpointMethod.returnType().is(ResponseEntity.class);
	}

	@Override
	public EndpointCallExecutable<ResponseEntity<T>, EndpointResponse<T>> create(EndpointMethod endpointMethod) {
		JavaType type = endpointMethod.returnType();

		Type responseType = type.parameterized() ? type.as(ParameterizedType.class).getActualTypeArguments()[0] : Object.class;

		return new ResponseEntityEndpointCallExecutable(JavaType.of(new SimpleParameterizedType(EndpointResponse.class, null, responseType)));
	}

	private class ResponseEntityEndpointCallExecutable implements EndpointCallExecutable<ResponseEntity<T>, EndpointResponse<T>> {

		private final JavaType returnType;

		private ResponseEntityEndpointCallExecutable(JavaType returnType) {
			this.returnType = returnType;
		}

		@Override
		public JavaType returnType() {
			return returnType;
		}

		@Override
		public ResponseEntity<T> execute(EndpointCall<EndpointResponse<T>> call, Object[] args) {
			EndpointResponse<T> response = call.execute();
			return endpointResponseConverter.convert(response);
		}
	}
}
