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

import java.lang.reflect.Type;
import java.util.Optional;

import com.github.ljtfreitas.restify.http.contract.ParameterSerializer;
import com.github.ljtfreitas.restify.http.contract.SimpleParameterSerializer;
import com.github.ljtfreitas.restify.reflection.JavaType;

public class EndpointMethodParameter {

	public enum EndpointMethodParameterType {
		PATH, HEADER, BODY, QUERY_STRING, ENDPOINT_CALLBACK;
	}

	private final int position;
	private final String name;
	private final JavaType javaType;
	private final EndpointMethodParameterType type;
	private final ParameterSerializer serializer;

	public EndpointMethodParameter(int position, String name, Type javaType) {
		this(position, name, javaType, EndpointMethodParameterType.PATH);
	}

	public EndpointMethodParameter(int position, String name, Type javaType, EndpointMethodParameterType type) {
		this(position, name, javaType, type, new SimpleParameterSerializer());
	}

	public EndpointMethodParameter(int position, String name, Type javaType, EndpointMethodParameterType type,
			ParameterSerializer serializer) {
		this(position, name, JavaType.of(javaType), type, serializer);
	}

	public EndpointMethodParameter(int position, String name, JavaType javaType, EndpointMethodParameterType type,
			ParameterSerializer serializer) {
		this.position = position;
		this.name = name;
		this.javaType = javaType;
		this.type = type;
		this.serializer = serializer;
	}

	public JavaType javaType() {
		return javaType;
	}

	public boolean is(String name) {
		return this.name.equals(name);
	}

	public String resolve(Object arg) {
		return Optional.ofNullable(serializer)
				.map(s -> s.serialize(name, javaType.unwrap(), arg))
					.orElseGet(() -> arg.toString());
	}

	public String name() {
		return name;
	}

	public int position() {
		return position;
	}

	public boolean body() {
		return type == EndpointMethodParameterType.BODY;
	}

	public boolean path() {
		return type == EndpointMethodParameterType.PATH;
	}

	public boolean header() {
		return type == EndpointMethodParameterType.HEADER;
	}

	public boolean query() {
		return type == EndpointMethodParameterType.QUERY_STRING;
	}

	public boolean callback() {
		return type == EndpointMethodParameterType.ENDPOINT_CALLBACK;
	}
}
