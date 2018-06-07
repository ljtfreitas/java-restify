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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.HypermediaBrowser;

class HypermediaLinkDeserializer extends JsonDeserializer<Link> implements ContextualDeserializer {

	private final HypermediaBrowser hypermediaBrowser;
	private final BeanProperty property;

	public HypermediaLinkDeserializer() {
		this(null, null);
	}

	public HypermediaLinkDeserializer(HypermediaBrowser hypermediaBrowser) {
		this(hypermediaBrowser, null);
	}

	public HypermediaLinkDeserializer(HypermediaBrowser hypermediaBrowser, BeanProperty property) {
		this.hypermediaBrowser = hypermediaBrowser;
		this.property = property;
	}

	@Override
	public Link deserialize(JsonParser jsonParser, DeserializationContext context)
			throws IOException, JsonProcessingException {

		String href = jsonParser.getText();

		return new Link(href, property.getName(), hypermediaBrowser);
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property)
			throws JsonMappingException {
		return new HypermediaLinkDeserializer(hypermediaBrowser, property);
	}
}
