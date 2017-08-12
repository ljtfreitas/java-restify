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

import static com.github.ljtfreitas.restify.http.util.Preconditions.nonNull;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Link {

	public static final String REL_SELF = "self";

	@JsonProperty
	private String href;

	@JsonProperty
	private String rel;

	@JsonProperty
	private String type;

	@JsonProperty
	private String title;

	private Map<String, String> properties = new HashMap<>();

	@Deprecated
	protected Link() {
	}

	public Link(@JsonProperty("href") String href) {
		this(href, REL_SELF);
	}

	public Link(String href, String rel) {
		this.href = href;
		this.rel = rel;
	}

	public Link(Link source, String rel) {
		this.href = source.href;
		this.type = source.type;
		this.title = source.title;
		this.properties = new LinkedHashMap<>(source.properties);
		this.rel = rel;
	}

	public String href() {
		return href;
	}

	public String rel() {
		return rel;
	}

	public Optional<String> title() {
		return Optional.ofNullable(title);
	}

	public Optional<String> type() {
		return Optional.ofNullable(type);
	}

	public boolean is(String rel) {
		return this.rel.equals(rel);
	}

	public Optional<String> property(String key) {
		return Optional.ofNullable(properties.get(key));
	}

	@JsonAnySetter
	private void anySetter(String name, String value) {
		this.properties.put(name, value);
	}

	public static Link self(String href) {
		return new Link(href, REL_SELF);
	}

	public static class Builder {

		private String href;
		private String rel;
		private String type;
		private String title;
		private Map<String, String> properties = new LinkedHashMap<>();

		public Builder href(String href) {
			this.href = href;
			return this;
		}

		public Builder href(URI href) {
			this.href = href.toString();
			return this;
		}

		public Builder href(URL href) {
			this.href = href.toString();
			return this;
		}

		public Builder rel(String rel) {
			this.rel = rel;
			return this;
		}

		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder templated() {
			properties.put("templated", "true");
			return this;
		}

		public Builder templated(boolean value) {
			properties.put("templated", Boolean.toString(value));
			return this;
		}

		public Builder name(String name) {
			properties.put("name", name);
			return this;
		}

		public Builder profile(String profile) {
			properties.put("profile", profile);
			return this;
		}

		public Builder hreflang(String hreflang) {
			properties.put("hreflang", hreflang);
			return this;
		}

		public Link build() {
			nonNull(href, "'href' link can't be null.");
			nonNull(rel, "'rel' link can't be null.");

			Link link = new Link(href, rel);
			link.title = title;
			link.type = type;
			link.properties = new LinkedHashMap<>(properties);

			return link;
		}
	}
}
