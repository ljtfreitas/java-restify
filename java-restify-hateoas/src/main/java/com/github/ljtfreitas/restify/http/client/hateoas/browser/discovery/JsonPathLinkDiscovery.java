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
package com.github.ljtfreitas.restify.http.client.hateoas.browser.discovery;

import java.util.Optional;

import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.message.ContentType;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;

public class JsonPathLinkDiscovery implements LinkDiscovery {

	private final String jsonPathTemplate;
	private final ContentType contentType;

	public JsonPathLinkDiscovery(String jsonPathTemplate) {
		this(jsonPathTemplate, ContentType.of("application/json"));
	}

	public JsonPathLinkDiscovery(String jsonPathTemplate, ContentType contentType) {
		this.jsonPathTemplate = jsonPathTemplate;
		this.contentType = contentType;
	}

	@Override
	public Optional<Link> find(String rel, RawResource resource) {
		if (rel.startsWith("$")) {
			return resolve(rel, resource.content());
		} else {
			return find(rel, resource.content());
		}
	}

	private Optional<Link> resolve(String expression, String content) {
		JsonPath reader = compile(expression);
		return convert(null, reader.read(content));
	}

	private Optional<Link> find(String rel, String json) {
		JsonPath reader = compile(String.format(jsonPathTemplate, rel));
		return convert(rel, reader.read(json));
	}

	private JsonPath compile(String expression) {
		return JsonPath.compile(expression);
	}

	private Optional<Link> convert(String rel, Object result) {
		if (result instanceof JSONArray) {
			JSONArray array = (JSONArray) result;
			return array.stream().findFirst().map(element -> new Link(element.toString(), rel));

		} else if (result instanceof JSONValue || result != null) {
			return Optional.of(new Link(result.toString(), rel));

		} else {
			return Optional.empty();
		}
	}

	@Override
	public boolean supports(ContentType contentType) {
		return this.contentType.compatible(contentType);
	}
}
