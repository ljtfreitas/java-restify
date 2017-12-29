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
package com.github.ljtfreitas.restify.http.client.message;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContentType {

	private static final Object WILDCARD_TYPE = "*";

	private final String type;
	private final String subtype;
	private final ContentTypeParameters parameters;

	private ContentType(String type, String subtype, ContentTypeParameters parameters) {
		this.type = type;
		this.subtype = subtype;
		this.parameters = parameters;
	}

	public String name() {
		return type + "/" + subtype;
	}

	public Optional<String> parameter(String name) {
		return parameters.get(name);
	}

	public ContentType append(String name, String value) {
		ContentTypeParameters newParameters = parameters.put(name, value);
		return new ContentType(type, subtype, newParameters);
	}

	public ContentTypeParameters parameters() {
		return parameters;
	}

	public boolean is(String contentType) {
		return doEquals(ContentType.of(contentType));
	}

	public boolean is(ContentType contentType) {
		return doEquals(contentType);
	}

	public boolean compatible(String contentType) {
		return doCompatible(ContentType.of(contentType));
	}

	public boolean compatible(ContentType contentType) {
		return doCompatible(contentType);
	}

	private boolean doCompatible(ContentType contentType) {
		return doEquals(contentType)
			|| (isWildcardType(this.type) || isWildcardType(contentType.type))
			|| (compatibleWithType(contentType.type) && compatibleWithSubtype(contentType.subtype));
	}

	private boolean compatibleWithType(String type) {
		return this.type.equals(type)
				|| (isWildcardType(this.type) || isWildcardType(type));
	}

	private boolean compatibleWithSubtype(String subtype) {
		if (this.subtype.equals(subtype)) {
			return true;
		} else if (isWildcardSubtype(this.subtype) || isWildcardSubtype(subtype)) {
			if (!isWildcardWithSuffix(this.subtype) && !isWildcardWithSuffix(subtype)) {
				return true;

			} else if (isWildcardWithSuffix(this.subtype) && isWildcardWithSuffix(subtype)) {
				return (extractSubtypeSuffix(this.subtype).equals(extractSubtypeSuffix(subtype)) &&
						(WILDCARD_TYPE.equals(extractSubtypeWithoutSuffix(this.subtype)) 
								|| WILDCARD_TYPE.equals(extractSubtypeWithoutSuffix(subtype))));
			}
		}

		return false;
	}

	private String extractSubtypeWithoutSuffix(String subtype) {
		return subtype.substring(0, subtype.indexOf("+"));
	}

	private String extractSubtypeSuffix(String subtype) {
		return subtype.substring(subtype.indexOf("+") + 1);
	}

	private boolean isWildcardWithSuffix(String subtype) {
		return subtype.contains("+");
	}
	
	private boolean isWildcardSubtype(String subtype) {
		return WILDCARD_TYPE.equals(subtype) || subtype.startsWith("*+");
	}
	
	private boolean isWildcardType(String type) {
		return WILDCARD_TYPE.equals(type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ContentType) {
			return doEquals((ContentType) obj);
		} else {
			return false;
		}
	}

	private boolean doEquals(ContentType that) {
		return this.type.equals(that.type)
			&& this.subtype.equals(that.subtype);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(type)
		  .append("/")
		  .append(subtype);

		if (!parameters.empty()) {
			sb.append("; ").append(parameters.toString());
		}

		return sb.toString();
	}

	public static ContentType of(String value) {
		String[] parts = value.split(";");

		String[] parameters = Arrays.copyOfRange(parts, 1, parts.length);

		String type = parts[0].substring(0, parts[0].indexOf("/")).toLowerCase();
		String subtype = parts[0].substring(parts[0].indexOf("/") + 1).toLowerCase();

		return new ContentType(type, subtype, ContentTypeParameters.of(parameters));
	}
	
	public static ContentType of(String type, String subtype) {
		return new ContentType(type, subtype, new ContentTypeParameters());		
	}

	public static class ContentTypeParameters {

		private final Map<String, String> parameters;

		private ContentTypeParameters() {
			this(Collections.emptyMap());
		}
		
		private ContentTypeParameters(Map<String, String> parameters) {
			this.parameters = new LinkedHashMap<>(parameters);
		}

		private Optional<String> get(String name) {
			return Optional.ofNullable(parameters.get(name));
		}

		private ContentTypeParameters put(String name, String value) {
			ContentTypeParameters newParameters = new ContentTypeParameters(parameters);
			newParameters.parameters.put(name, value);
			return newParameters;
		}

		public boolean empty() {
			return parameters.isEmpty();
		}

		@Override
		public String toString() {
			return parameters.entrySet().stream()
					.map(p -> p.getKey() + "=" + p.getValue())
						.collect(Collectors.joining("; "));
		}

		private static ContentTypeParameters of(String[] parameters) {
			Map<String, String> mapOfParameters = new LinkedHashMap<>();

			Arrays.stream(parameters).map(p -> p.split("=")).filter(p -> p.length == 2)
					.forEach(p -> mapOfParameters.put(p[0].trim(), p[1].trim()));

			return new ContentTypeParameters(mapOfParameters);
		}
	}
}