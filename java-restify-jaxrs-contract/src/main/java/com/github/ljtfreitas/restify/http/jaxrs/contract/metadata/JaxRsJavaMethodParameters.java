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
package com.github.ljtfreitas.restify.http.jaxrs.contract.metadata;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.HeaderParam;

class JaxRsJavaMethodParameters {

	private final List<JaxRsJavaMethodParameterMetadata> parameters;

	public JaxRsJavaMethodParameters(Method javaMethod, Class<?> javaType) {
		this.parameters = Arrays.stream(javaMethod.getParameters())
				.map(p -> new JaxRsJavaMethodParameterMetadata(p, javaType))
					.collect(Collectors.toList());
	}

	public int size() {
		return parameters.size();
	}

	public JaxRsJavaMethodParameterMetadata get(int position) {
		return parameters.get(position);
	}

	public JaxRsEndpointHeader[] headers() {
		return parameters.stream().filter(p -> p.header())
			.map(p -> p.annotation(HeaderParam.class))
				.filter(a -> a.value() != null && !"".equals(a.value()))
					.map(a -> new JaxRsEndpointHeader(a.value(), "{" + a.value() + "}"))
						.toArray(s -> new JaxRsEndpointHeader[s]);
	}
}
