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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.github.ljtfreitas.restify.http.contract.Form;
import com.github.ljtfreitas.restify.http.contract.Parameters;

public class EndpointMethodQueryParametersSerializer implements EndpointMethodParameterSerializer {

	private final EndpointMethodFormObjectParameterSerializer formObjectSerializer;

	public EndpointMethodQueryParametersSerializer() {
		this(new EndpointMethodFormObjectParameterSerializer());
	}

	public EndpointMethodQueryParametersSerializer(EndpointMethodFormObjectParameterSerializer formObjectSerializer) {
		this.formObjectSerializer = formObjectSerializer;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public String serialize(String name, Type type, Object source) {
		if (source == null) {
			return null;

		} else if (supported(type, source)) {
			if (isParameters(source)) {
				return serializeAsParameters((Parameters) source);

			} else if (isSupportedMap(type, source)) {
				return serializeAsMap((Map) source);

			} else if (isFormObject(source)) {
				return serializeAsFormObject(name, type, source);
			}
		}

		throw new IllegalArgumentException("EndpointMethodQueryParametersSerializer does no support a parameter of type " + source.getClass());
	}

	public boolean supports(Type type, Object source) {
		return supported(type, source);
	}

	private boolean supported(Type type, Object source) {
		return isParameters(source) || isSupportedMap(type, source) || isFormObject(source);
	}

	private boolean isParameters(Object source) {
		return source instanceof Parameters;
	}

	private boolean isSupportedMap(Type type, Object source) {
		return source instanceof Map && supportedMapKey(type);
	}

	private boolean supportedMapKey(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;

			Type mapKeyType = parameterizedType.getActualTypeArguments()[0];

			if (mapKeyType == String.class) {
				return true;

			} else {
				throw new IllegalStateException("Your Map parameter must have a key of String type.");
			}
		} else {
			return true;
		}
	}

	private String serializeAsFormObject(String name, Type type, Object source) {
		return formObjectSerializer.serialize(name, type, source);
	}

	private boolean isFormObject(Object source) {
		return source.getClass().getAnnotation(Form.class) != null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String serializeAsMap(Map source) {
		return serializeAsParameters(Parameters.of(source));
	}

	private String serializeAsParameters(Parameters source) {
		return source.queryString();
	}
}
