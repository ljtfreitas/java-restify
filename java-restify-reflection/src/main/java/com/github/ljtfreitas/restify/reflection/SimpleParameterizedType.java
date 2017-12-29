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
package com.github.ljtfreitas.restify.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class SimpleParameterizedType implements ParameterizedType {

	private final Type rawType;
	private final Type ownerType;
	private final Type[] typeArguments;

	public SimpleParameterizedType(Type rawType, Type ownerType, Type...typeArguments) {
		this.rawType = rawType;
		this.ownerType = ownerType;
		this.typeArguments = typeArguments;
	}

	@Override
	public Type[] getActualTypeArguments() {
		return typeArguments;
	}

	@Override
	public Type getRawType() {
		return rawType;
	}

	@Override
	public Type getOwnerType() {
		return ownerType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(rawType, ownerType, typeArguments);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ParameterizedType) {
			ParameterizedType that = (ParameterizedType) obj;

			return Objects.equals(rawType, that.getRawType())
				&& Objects.equals(ownerType, that.getOwnerType())
				&& Arrays.equals(typeArguments, that.getActualTypeArguments());

		} else return false;
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("SimpleParameterizedType: [")
				.append("Raw Type: ")
					.append(rawType)
				.append(", ")
				.append("Owner Type: ")
					.append(ownerType)
				.append(", ")
				.append("Type Arguments: ")
					.append(Arrays.toString(typeArguments))
			.append("]");

		return report.toString();

	}
}
