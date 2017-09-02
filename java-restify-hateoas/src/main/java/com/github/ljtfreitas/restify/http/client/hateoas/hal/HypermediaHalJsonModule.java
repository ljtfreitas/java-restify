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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.ljtfreitas.restify.http.client.hateoas.Embedded;
import com.github.ljtfreitas.restify.http.client.hateoas.Links;
import com.github.ljtfreitas.restify.http.client.hateoas.Resource;

class HypermediaHalJsonModule extends SimpleModule {

	private static final long serialVersionUID = 1L;

	public HypermediaHalJsonModule() {
		setMixInAnnotation(Resource.class, HalResourceMixIn.class);
	}

	abstract class HalResourceMixIn<T> extends Resource<T> {

		HalResourceMixIn() {
			super(null);
		}

		@JsonProperty("_links")
		@JsonInclude(Include.NON_EMPTY)
		@JsonDeserialize(using = HypermediaHalLinksDeserializer.class)
		@JsonSerialize(using = HypermediaHalLinksSerializer.class)
		private Links links = new Links();

		@JsonProperty(value = "_embedded", access = Access.WRITE_ONLY)
		@JsonDeserialize(using = HypermediaHalEmbeddedDeserializer.class)
		private Embedded embedded;
	}

}
