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
import java.util.LinkedList;
import java.util.stream.Collectors;

public class EndpointPathBuilder {

	private final Collection<String> parts;

	public EndpointPathBuilder() {
		this.parts = new LinkedList<>();
	}

	private EndpointPathBuilder(Collection<String> parts) {
		this.parts = parts;
	}

	public EndpointPathBuilder append(String part) {
		Collection<String> list = new LinkedList<>(this.parts);

		if (part != null && !part.trim().isEmpty()) {
			list.add(removeSlashOnTheStart(removeSlashOnTheEnd(part)));
		}

		return new EndpointPathBuilder(list);
	}

	public String build() {
		return parts.stream().collect(Collectors.joining("/"));
	}

	private String removeSlashOnTheStart(String part) {
		return part.startsWith("/") ? part.substring(1, part.length()) : part;
	}

	private String removeSlashOnTheEnd(String part) {
		return part.endsWith("/") ? part.substring(0, part.length() - 1) : part;
	}
}