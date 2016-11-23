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

import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodQueryParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.metadata.EndpointMethodQueryParametersSerializer;
import com.github.ljtfreitas.restify.http.contract.metadata.SimpleEndpointMethodParameterSerializer;
import com.github.ljtfreitas.restify.http.spring.contract.metadata.reflection.SpringWebJavaMethodParameterMetadata;

public class SpringWebEndpointMethodParameterSerializer {

	public static Class<? extends EndpointMethodParameterSerializer> of(SpringWebJavaMethodParameterMetadata parameter) {
		if (parameter.query()) {
			return SpringWebQueryParameterSerializer.class;
		} else {
			return SimpleEndpointMethodParameterSerializer.class;
		}
	}

	public static class SpringWebQueryParameterSerializer implements EndpointMethodParameterSerializer {

		private final EndpointMethodQueryParameterSerializer queryParameterSerializer = new EndpointMethodQueryParameterSerializer();

		private final EndpointMethodQueryParametersSerializer queryParametersSerializer = new EndpointMethodQueryParametersSerializer();

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
