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
import java.util.Collection;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.message.Encoding;

public class LinkURITemplate {

	private final String source;

	public LinkURITemplate(String source) {
		this.source = source;
	}

	public URI expand() {
		return doExpand(LinkURITemplateParameters.empty());
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

			Collection<String> variables = Arrays.stream(names)
					.map(name -> new TemplateVariable(name, type))
						.map(variable -> expand(variable, parameters))
							.filter(value -> value != null && !value.trim().isEmpty())
								.collect(Collectors.toList());

			String expanded = variables.isEmpty() ? "" : variables.stream().collect(Collectors.joining(type.separator, type.starter, ""));

			matcher.appendReplacement(builder, expanded);
		}

		matcher.appendTail(builder);

		return builder.toString();
	}

	private String expand(TemplateVariable variable, LinkURITemplateParameters parameters) {
		return parameters.get(variable.name).map(parameter -> variable.resolve(parameter)).orElse("");
	}

	private class TemplateVariable {

		private final String name;
		private final TemplateVariableType type;

		private TemplateVariable(String name, TemplateVariableType type) {
			this.name = name;
			this.type = type;
		}

		private String resolve(String value) {
			return type.resolve(name, value);
		}
	}

	private enum TemplateVariableType {
		PATH_VARIABLE("", ",") {
			@Override
			String resolve(String name, String value) {
				return value;
			}
		},
		PATH_SEGMENT("/", "/", "/") {
			@Override
			String resolve(String name, String value) {
				return value;
			}
		},
		REQUEST_PARAMETER("?", "&", "?") {
			@Override
			String resolve(String name, String value) {
				return String.format("%s=%s", name, Encoding.UTF_8.encode(value));
			}
		},
		REQUEST_PARAMETER_CONTINUED("&", "&", "&") {
			@Override
			String resolve(String name, String value) {
				return REQUEST_PARAMETER.resolve(name, value);
			}
		},
		FRAGMENT("#", ",", "#") {
			@Override
			String resolve(String name, String value) {
				return value;
			}
		},
		RESERVED("+", ",") {
			@Override
			String resolve(String name, String value) {
				return value;
			}
		};

		private final String operator;
		private final String separator;
		private final String starter;

		private TemplateVariableType(String operator, String separator) {
			this(operator, separator, "");
		}

		private TemplateVariableType(String operator, String separator, String starter) {
			this.operator = operator;
			this.separator = separator;
			this.starter = starter;
		}

		private static TemplateVariableType of(String key) {
			return Arrays.stream(values())
						.filter(t -> t.operator.equals(key))
							.findFirst()
								.orElseThrow(() -> new IllegalArgumentException("Unsupported variable type: [" + key + "]"));
		}

		abstract String resolve(String name, String value);
	}

	private static class URITemplateMatcher {

		private static final Pattern URI_TEMPLATE_PATTERN = Pattern.compile("\\{([\\+\\?\\&#/]?)([\\w\\,]+)\\}");

		public static Matcher matches(String source) {
			return URI_TEMPLATE_PATTERN.matcher(source);
		}
	}
}
