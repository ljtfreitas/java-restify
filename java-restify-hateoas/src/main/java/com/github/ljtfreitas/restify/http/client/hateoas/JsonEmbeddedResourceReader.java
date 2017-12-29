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

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.ljtfreitas.restify.util.Tryable;

public class JsonEmbeddedResourceReader {

	private final ObjectCodec codec;
	private final TypeFactory typeFactory;

	public JsonEmbeddedResourceReader(ObjectCodec codec, TypeFactory typeFactory) {
		this.codec = codec;
		this.typeFactory = typeFactory;
	}

	public <T> Resource<T> readAs(Class<? extends T> type, TreeNode tree) {
		return Tryable.of(() -> this.readAs(typeFactory.constructParametricType(Resource.class, type), tree));
	}

	public <T> Collection<Resource<T>> readAsCollectionOf(Class<? extends T> type, TreeNode tree) {
		JavaType resourceType = typeFactory.constructParametricType(Resource.class, type);
		return Tryable.of(() -> this.readAs(typeFactory.constructParametricType(Collection.class, resourceType), tree));
	}

	private <T> T readAs(ResolvedType resolvedType, TreeNode tree) throws IOException {
		try (JsonParser parser = tree.traverse(codec)) {
			return codec.readValue(parser, resolvedType);
		}
	}
}
