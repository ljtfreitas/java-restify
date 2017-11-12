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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

public class SimpleGenericArrayType implements GenericArrayType {

	private final Type componentType;

	public SimpleGenericArrayType(Type componentType) {
		this.componentType = componentType;
	}

	@Override
	public Type getGenericComponentType() {
		return componentType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(componentType);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GenericArrayType) {
			return Objects.equals(componentType, ((GenericArrayType) obj).getGenericComponentType());

		} else
			return super.equals(obj);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report.append("SimpleGenericArrayType: [")
			.append("Component Type: ")
				.append(componentType)
			.append("]");

		return report.toString();
	}

}
