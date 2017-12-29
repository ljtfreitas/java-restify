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
package com.github.ljtfreitas.restify.http.spring.contract.metadata;

import java.lang.reflect.Type;

import com.github.ljtfreitas.restify.http.contract.ParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.QueryParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.QueryParametersSerializer;
import com.github.ljtfreitas.restify.http.contract.SimpleParameterSerializer;

class SpringWebEndpointMethodParameterSerializer {

	static Class<? extends ParameterSerializer> of(SpringWebJavaMethodParameterMetadata parameter) {
		if (parameter.query()) {
			return SpringWebQueryParameterSerializer.class;
		} else {
			return SimpleParameterSerializer.class;
		}
	}

	public static class SpringWebQueryParameterSerializer implements ParameterSerializer {

		private final QueryParameterSerializer queryParameterSerializer = new QueryParameterSerializer();

		private final QueryParametersSerializer queryParametersSerializer = new QueryParametersSerializer();

		@Override
		public String serialize(String name, Type type, Object source) {
			if (queryParametersSerializer.supports(type, source)) {
				return queryParametersSerializer.serialize(name, type, source);
			} else {
				return queryParameterSerializer.serialize(name, type, source);
			}
		}
	}
}
