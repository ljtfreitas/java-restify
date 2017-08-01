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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class LinksDeserializer extends ContainerDeserializerBase<List<Link>> {

	private static final long serialVersionUID = 1L;

	public LinksDeserializer() {
		super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Link.class));
	}

	@Override
	public JavaType getContentType() {
		return null;
	}

	@Override
	public JsonDeserializer<Object> getContentDeserializer() {
		return null;
	}

	@Override
	public List<Link> deserialize(JsonParser jsonParser, DeserializationContext context)
			throws IOException, JsonProcessingException {

		List<Link> links = new ArrayList<Link>();

		while (!JsonToken.END_OBJECT.equals(jsonParser.nextToken())) {
			if (!JsonToken.FIELD_NAME.equals(jsonParser.getCurrentToken())) {
				throw new JsonParseException(jsonParser, "Expected link name.");
			}

			String relation = jsonParser.getText();

			if (JsonToken.START_ARRAY.equals(jsonParser.nextToken())) {
				while (!JsonToken.END_ARRAY.equals(jsonParser.nextToken())) {
					links.add(readToLink(relation, jsonParser));
				}
			} else {
				links.add(readToLink(relation, jsonParser));
			}
		}

		return links;
	}
	
	private Link readToLink(String relation, JsonParser jsonParser) throws IOException {
		Link link = jsonParser.readValueAs(Link.class);
		return new Link(link, relation);
	}
}
