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
package com.github.ljtfreitas.restify.http.client.hateoas;

import java.util.Collection;

import com.fasterxml.jackson.core.TreeNode;

public class JsonEmbeddedResource implements EmbeddedResource {

	private final String name;
	private final TreeNode tree;
	private final JsonEmbeddedResourceReader reader;

	public JsonEmbeddedResource(String name, TreeNode tree, JsonEmbeddedResourceReader reader) {
		this.name = name;
		this.tree = tree;
		this.reader = reader;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public <T> Resource<T> as(Class<? extends T> type) {
		return reader.readAs(type, tree);
	}

	@Override
	public <T> Collection<Resource<T>> collectionOf(Class<? extends T> type) {
		return reader.readAsCollectionOf(type, tree);
	}

	@Override
	public String toString() {
		return tree.toString();
	}
}