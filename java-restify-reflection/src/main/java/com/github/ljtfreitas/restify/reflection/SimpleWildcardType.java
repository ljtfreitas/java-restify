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

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;

public class SimpleWildcardType implements WildcardType {

	private final Type[] upperBounds;
	private final Type[] lowerBounds;

	public SimpleWildcardType(Type[] upperBounds, Type[] lowerBounds) {
		this.upperBounds = upperBounds;
		this.lowerBounds = lowerBounds;
	}

	@Override
	public Type[] getUpperBounds() {
		return upperBounds;
	}

	@Override
	public Type[] getLowerBounds() {
		return lowerBounds;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(upperBounds, lowerBounds);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WildcardType) {
			WildcardType that = (WildcardType) obj;

			return Arrays.equals(upperBounds, that.getUpperBounds())
				&& Arrays.equals(lowerBounds, that.getLowerBounds());

		} else return super.equals(obj);
	}

	@Override
	public String toString() {
		StringBuilder report = new StringBuilder();

		report
			.append("SimpleWildcardType: [")
				.append("Upper Bounds: ")
					.append(Arrays.toString(upperBounds))
				.append(", ")
				.append("Lower Bounds: ")
					.append(lowerBounds)
			.append("]");

		return report.toString();
	}
}
