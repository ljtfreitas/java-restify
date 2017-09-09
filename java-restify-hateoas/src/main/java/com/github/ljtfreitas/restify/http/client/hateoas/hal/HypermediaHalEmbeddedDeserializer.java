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
package com.github.ljtfreitas.restify.http.client.hateoas.hal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.ljtfreitas.restify.http.client.hateoas.Embedded;
import com.github.ljtfreitas.restify.http.client.hateoas.EmbeddedResource;
import com.github.ljtfreitas.restify.http.client.hateoas.JsonEmbeddedResource;
import com.github.ljtfreitas.restify.http.client.hateoas.JsonEmbeddedResourceReader;

class HypermediaHalEmbeddedDeserializer extends JsonDeserializer<Embedded> {

	@Override
	public Embedded deserialize(JsonParser jsonParser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		ObjectCodec codec = jsonParser.getCodec();

		TreeNode tree = codec.readTree(jsonParser);

		context.getTypeFactory();

		Collection<EmbeddedResource> elements = new ArrayList<>();

		tree.fieldNames()
			.forEachRemaining(field -> elements.add(new JsonEmbeddedResource(field, tree.get(field),
					new JsonEmbeddedResourceReader(codec, context.getTypeFactory()))));

        return new Embedded(elements);
	}

}
