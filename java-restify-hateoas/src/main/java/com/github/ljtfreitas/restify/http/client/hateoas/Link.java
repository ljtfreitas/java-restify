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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.LinkBrowser;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.LinkBrowser.LinkBrowserTraverson;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.LinkURITemplateParameters;

@JsonPropertyOrder(value = {"href", "rel", "type", "title"})
public class Link {

	public static final String REL_SELF = "self";

	@JsonProperty
	private String href;

	@JsonProperty
	private String rel;

	@JsonInclude(value = Include.NON_EMPTY)
	@JsonProperty
	private String type;

	@JsonInclude(value = Include.NON_EMPTY)
	@JsonProperty
	private String title;

	private Map<String, String> properties = new HashMap<>();

	@JsonBackReference
	private Resource<?> owner;

	@JsonIgnore
	private LinkBrowser browser;

	@Deprecated
	protected Link() {
	}

	public Link(String href, String rel) {
		this(href, rel, Collections.emptyMap(), null, null);
	}

	public Link(String href, String rel, Map<String, String> properties, Resource<?> owner, LinkBrowser browser) {
		this.href = nonNull(href, "Link href cannot be null.");
		this.rel = rel;
		this.title = properties.getOrDefault("title", null);
		this.type = properties.getOrDefault("type", null);
		this.properties = properties;
		this.owner = owner;
		this.browser = browser;
	}

	public Link(Link source) {
		this(source, source.browser);
	}

	public Link(Link source, LinkBrowser browser) {
		this(source, source.rel, browser);
	}

	public Link(Link source, String rel, LinkBrowser browser) {
		this.href = source.href;
		this.type = source.type;
		this.title = source.title;
		this.properties = new HashMap<>(source.properties);
		this.owner = source.owner;
		this.rel = rel;
		this.browser = browser;
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

	public boolean templated() {
		Optional<String> templated = findProperty("templated");
		return templated.isPresent() && templated.map(Boolean::valueOf).get();
	}

	public boolean deprecation() {
		Optional<String> deprecation = findProperty("deprecation");
		return deprecation.isPresent() && deprecation.map(Boolean::valueOf).get();
	}

	public Optional<String> property(String key) {
		return findProperty(key);
	}

	private Optional<String> findProperty(String key) {
		return Optional.ofNullable(properties.get(key));
	}

	@JsonAnySetter
	private void anySetter(String name, String value) {
		this.properties.put(name, value);
	}

	public LinkBrowserTraverson follow() {
		nonNull(browser, "Cannot follow this link [" + href + "], because LinkBrowser has not set.");
		return browser.follow(this, owner == null ? LinkURITemplateParameters.empty() : LinkURITemplateParameters.of(owner));
	}

	@Override
	public String toString() {
		return String.format("Link: [" + href + "]");
	}

	public static Link self(String href) {
		return new Link(href, REL_SELF, Collections.emptyMap(), null, null);
	}

	public static Link self(String href, LinkBrowser linkBrowser) {
		return new Link(href, REL_SELF, Collections.emptyMap(), null, linkBrowser);
	}
}
