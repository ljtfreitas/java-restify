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
package com.github.ljtfreitas.restify.http.contract.metadata;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

class QueryParameterResolver {

	private final Collection<EndpointMethodParameter> parameters;

	QueryParameterResolver(Collection<EndpointMethodParameter> parameters) {
		this.parameters = parameters;
	}

	String resolve(Object[] args) {
		String query = parameters.stream().filter(EndpointMethodParameter::query)
			.map(p -> resolve(p, args))
				.filter(p -> !"".equals(p))
					.collect(Collectors.joining("&"));

		return Optional.ofNullable(query)
				.map(String::trim)
					.filter(s -> !s.isEmpty())
						.orElse("");
	}

	private String resolve(EndpointMethodParameter parameter, Object[] args) {
		return args.length < parameter.position() ? "" :
				Optional.ofNullable(args[parameter.position()])
					.map(arg -> parameter.resolve(arg))
						.orElse("");
	}
}
