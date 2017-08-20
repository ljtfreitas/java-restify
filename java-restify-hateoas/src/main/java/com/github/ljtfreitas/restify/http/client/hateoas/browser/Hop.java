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
package com.github.ljtfreitas.restify.http.client.hateoas.browser;

import java.util.Map;

public class Hop {

	private final String rel;
	private final LinkURITemplateParameters parameters;

	private Hop(String rel) {
		this(rel, new LinkURITemplateParameters());
	}

	private Hop(String rel, LinkURITemplateParameters parameters) {
		this.rel = rel;
		this.parameters = parameters;
	}

	public String rel() {
		return rel;
	}

	public LinkURITemplateParameters parameters() {
		return parameters;
	}

	public Hop with(String name, String value) {
		LinkURITemplateParameters parameters = new LinkURITemplateParameters(this.parameters);
		parameters.put(name, value);
		return new Hop(rel, parameters);
	}

	public static Hop rel(String rel) {
		return new Hop(rel);
	}

	public static Hop rel(String rel, Map<String, String> parameters) {
		return new Hop(rel, new LinkURITemplateParameters(parameters));
	}

	public static Hop rel(String rel, LinkURITemplateParameters parameters) {
		return new Hop(rel, parameters);
	}
}