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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.HypermediaBrowser;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.HypermediaBrowser.HypermediaBrowserTraverson;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.LinkURITemplateParameter;
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

	@JsonIgnore
	private HypermediaBrowser hypermediaBrowser;

	@Deprecated
	protected Link() {
	}

	public Link(URI href) {
		this(href, REL_SELF);
	}

	public Link(URL href) {
		this(href, REL_SELF);
	}

	public Link(String href) {
		this(href, REL_SELF);
	}

	public Link(URI href, String rel) {
		this(href, rel, Collections.emptyMap());
	}

	public Link(URL href, String rel) {
		this(href, rel, Collections.emptyMap());
	}

	public Link(String href, String rel) {
		this(href, rel, Collections.emptyMap());
	}

	public Link(URI href, String rel, Map<String, String> properties) {
		this(href, rel, properties, null);
	}

	public Link(URL href, String rel, Map<String, String> properties) {
		this(href, rel, properties, null);
	}

	public Link(String href, String rel, Map<String, String> properties) {
		this(href, rel, properties, null);
	}

	public Link(URI href, String rel, HypermediaBrowser hypermediaBrowser) {
		this(href, rel, Collections.emptyMap(), hypermediaBrowser);
	}

	public Link(URL href, String rel, HypermediaBrowser hypermediaBrowser) {
		this(href, rel, Collections.emptyMap(), hypermediaBrowser);
	}

	public Link(String href, String rel, HypermediaBrowser hypermediaBrowser) {
		this(href, rel, Collections.emptyMap(), hypermediaBrowser);
	}

	public Link(URI href, Map<String, String> properties) {
		this(href, REL_SELF, properties);
	}

	public Link(URL href, Map<String, String> properties) {
		this(href, REL_SELF, properties);
	}

	public Link(String href, Map<String, String> properties) {
		this(href, REL_SELF, properties);
	}

	public Link(URI href, HypermediaBrowser hypermediaBrowser) {
		this(href, REL_SELF, hypermediaBrowser);
	}

	public Link(URL href, HypermediaBrowser hypermediaBrowser) {
		this(href, REL_SELF, hypermediaBrowser);
	}

	public Link(String href, HypermediaBrowser hypermediaBrowser) {
		this(href, REL_SELF, hypermediaBrowser);
	}

	public Link(URI href, Map<String, String> properties, HypermediaBrowser hypermediaBrowser) {
		this(href, REL_SELF, properties, hypermediaBrowser);
	}

	public Link(URL href, Map<String, String> properties, HypermediaBrowser hypermediaBrowser) {
		this(href, REL_SELF, properties, hypermediaBrowser);
	}

	public Link(String href, Map<String, String> properties, HypermediaBrowser hypermediaBrowser) {
		this(href, REL_SELF, properties, hypermediaBrowser);
	}

	public Link(URI href, String rel, Map<String, String> properties, HypermediaBrowser hypermediaBrowser) {
		this(nonNull(href, "Link href cannot be null.").toString(), rel, properties, hypermediaBrowser);
	}

	public Link(URL href, String rel, Map<String, String> properties, HypermediaBrowser hypermediaBrowser) {
		this(nonNull(href, "Link href cannot be null.").toString(), rel, properties, hypermediaBrowser);
	}

	public Link(String href, String rel, Map<String, String> properties, HypermediaBrowser hypermediaBrowser) {
		this.href = nonNull(href, "Link href cannot be null.");
		this.rel = rel;
		this.title = properties.getOrDefault("title", null);
		this.type = properties.getOrDefault("type", null);
		this.properties = properties;
		this.hypermediaBrowser = hypermediaBrowser;
	}

	public Link(Link source) {
		this(source, source.hypermediaBrowser);
	}

	public Link(Link source, HypermediaBrowser hypermediaBrowser) {
		this(source, source.rel, hypermediaBrowser);
	}

	public Link(Link source, String rel, HypermediaBrowser hypermediaBrowser) {
		this.href = source.href;
		this.type = source.type;
		this.title = source.title;
		this.properties = new HashMap<>(source.properties);
		this.rel = rel;
		this.hypermediaBrowser = hypermediaBrowser;
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

	public HypermediaBrowserTraverson follow() {
		return doFollow(LinkURITemplateParameters.empty());
	}

	public HypermediaBrowserTraverson follow(LinkURITemplateParameters parameters) {
		return doFollow(parameters);
	}

	public HypermediaBrowserTraverson follow(LinkURITemplateParameter... parameters) {
		return doFollow(new LinkURITemplateParameters(parameters));
	}

	public HypermediaBrowserTraverson follow(Map<String, String> parameters) {
		return doFollow(new LinkURITemplateParameters(parameters));
	}

	private HypermediaBrowserTraverson doFollow(LinkURITemplateParameters parameters) {
		nonNull(hypermediaBrowser, "Cannot follow this link [" + href + "], because LinkBrowser has not set.");
		return hypermediaBrowser.follow(this, parameters);
	}

	@Override
	public String toString() {
		return String.format("Link: [%s:%s]", rel, href);
	}

	public static Link self(String href) {
		return new Link(href, REL_SELF, Collections.emptyMap(), null);
	}

	public static Link self(String href, HypermediaBrowser hypermediaBrowser) {
		return new Link(href, REL_SELF, Collections.emptyMap(), hypermediaBrowser);
	}
}
