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

import static com.github.ljtfreitas.restify.util.Preconditions.nonNull;

import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.ljtfreitas.restify.http.client.hateoas.browser.HypermediaBrowser;

public class LinkBuilder {

	private String href;
	private String rel;
	private Map<String, String> properties = new LinkedHashMap<>();
	private HypermediaBrowser hypermediaBrowser;

	public LinkBuilder href(String href) {
		this.href = href;
		return this;
	}

	public LinkBuilder href(URI href) {
		this.href = href.toString();
		return this;
	}

	public LinkBuilder href(URL href) {
		this.href = href.toString();
		return this;
	}

	public LinkBuilder rel(String rel) {
		this.rel = rel;
		return this;
	}

	public LinkBuilder type(String type) {
		properties.put("type", type);
		return this;
	}

	public LinkBuilder title(String title) {
		properties.put("title", title);
		return this;
	}

	public LinkBuilder templated() {
		properties.put("templated", "true");
		return this;
	}

	public LinkBuilder templated(boolean value) {
		properties.put("templated", Boolean.toString(value));
		return this;
	}

	public LinkBuilder name(String name) {
		properties.put("name", name);
		return this;
	}

	public LinkBuilder profile(String profile) {
		properties.put("profile", profile);
		return this;
	}

	public LinkBuilder hreflang(String hreflang) {
		properties.put("hreflang", hreflang);
		return this;
	}

	public LinkBuilder browser(HypermediaBrowser hypermediaBrowser) {
		this.hypermediaBrowser = hypermediaBrowser;
		return this;
	}

	public Link build() {
		nonNull(href, "'href' link can't be null.");

		return new Link(href, rel, properties, hypermediaBrowser);
	}
}
