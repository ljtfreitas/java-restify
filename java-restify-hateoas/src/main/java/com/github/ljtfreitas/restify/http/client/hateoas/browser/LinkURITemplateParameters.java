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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LinkURITemplateParameters implements Iterable<LinkURITemplateParameter> {

	private final Map<String, String> parameters;

	public LinkURITemplateParameters() {
		this.parameters = new HashMap<>();
	}

	public LinkURITemplateParameters(Map<String, String> parameters) {
		this.parameters = new HashMap<>(parameters);
	}

	public LinkURITemplateParameters(LinkURITemplateParameters source) {
		this(source.parameters);
	}

	public LinkURITemplateParameters(LinkURITemplateParameter... parameters) {
		this(Arrays.stream(parameters)
				.collect(Collectors.toMap(LinkURITemplateParameter::name, LinkURITemplateParameter::value)));
	}

	public Optional<String> get(String name) {
		return Optional.ofNullable(parameters.get(name));
	}

	public LinkURITemplateParameters put(String name, String value) {
		LinkURITemplateParameters parameters = new LinkURITemplateParameters(this.parameters);
		parameters.add(name, value);
		return parameters;
	}

	public LinkURITemplateParameters put(String name, Object value) {
		LinkURITemplateParameters parameters = new LinkURITemplateParameters(this.parameters);
		parameters.add(name, value.toString());
		return parameters;
	}

	public LinkURITemplateParameters put(LinkURITemplateParameter parameter) {
		LinkURITemplateParameters parameters = new LinkURITemplateParameters(this.parameters);
		parameters.add(parameter.name(), parameter.value());
		return parameters;
	}

	private void add(String name, String value) {
		parameters.put(name, value);
	}

	@Override
	public Iterator<LinkURITemplateParameter> iterator() {
		return parameters.entrySet().stream()
				.map(e -> LinkURITemplateParameter.using(e.getKey(), e.getValue()))
					.collect(Collectors.toList())
						.iterator();
	}

	@Override
	public String toString() {
		return parameters.toString();
	}

	public static LinkURITemplateParameters empty() {
		return new LinkURITemplateParameters();
	}
}
