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

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.ljtfreitas.restify.http.client.charset.Encoding;

public class LinkURITemplate {

	private final String source;

	public LinkURITemplate(String source) {
		this.source = source;
	}

	public URI expand() {
		return URI.create(source);
	}

	public URI expand(Map<String, String> parameters) {
		return doExpand(new LinkURITemplateParameters(parameters));
	}

	public URI expand(LinkURITemplateParameters parameters) {
		return doExpand(parameters);
	}

	private URI doExpand(LinkURITemplateParameters parameters) {
		String resolved = doResolve(parameters);

		return URI.create(resolved);
	}

	private String doResolve(LinkURITemplateParameters parameters) {
		StringBuffer builder = new StringBuffer();

		Matcher matcher = URITemplateMatcher.matches(source);

		while (matcher.find()) {
			MatchResult match = matcher.toMatchResult();

			TemplateVariableType type = TemplateVariableType.of(match.group(1));

			String[] names = match.group(2).split(",");

			Arrays.stream(names).map(name -> new TemplateVariable(name, type))
				.forEach(variable -> matcher.appendReplacement(builder, replace(variable, parameters)));
		}

		matcher.appendTail(builder);

		return builder.toString();
	}

	private String replace(TemplateVariable variable, LinkURITemplateParameters parameters) {
		String name = variable.name;
		return parameters.find(name).map(p -> variable.resolve(p)).orElseGet(() -> "");
	}

	private class TemplateVariable {

		private final String name;
		private final TemplateVariableType type;

		private TemplateVariable(String name, TemplateVariableType type) {
			this.name = name;
			this.type = type;
		}

		public String resolve(String value) {
			return type.resolve(name, value);
		}
	}

	private enum TemplateVariableType {
		PATH_VARIABLE(""),
		PATH_SEGMENT("/"),
		REQUEST_PARAMETER("?") {
			@Override
			public String resolve(String name, String value) {
				return "?" + name + "=" + Encoding.UTF_8.encode(value);
			}
		},
		REQUEST_PARAMETER_CONTINUED("&") {
			@Override
			public String resolve(String name, String value) {
				return "&" + name + "=" + Encoding.UTF_8.encode(value);
			}
		},
		FRAGMENT("#"),
		RESERVED("+") {
			@Override
			public String resolve(String name, String value) {
				return value;
			}
		};

		private final String key;

		private TemplateVariableType(String key) {
			this.key = key;
		}

		public String resolve(String name, String value) {
			return key + value;
		}

		private static TemplateVariableType of(String key) {
			return Arrays.stream(values())
						.filter(t -> t.key.equals(key))
							.findFirst()
								.orElseThrow(() -> new IllegalArgumentException("Unsupported variable type: [" + key + "]"));
		}

		@Override
		public String toString() {
			return key;
		}
	}

	private static class URITemplateMatcher {

		private static final Pattern URI_TEMPLATE_PATTERN = Pattern.compile("\\{([\\+\\?\\&#/]?)([\\w\\,]+)\\}");

		public static Matcher matches(String source) {
			return URI_TEMPLATE_PATTERN.matcher(source);
		}
	}
}
