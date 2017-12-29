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
package com.github.ljtfreitas.restify.http.client.hateoas.hal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.ljtfreitas.restify.http.client.hateoas.Link;
import com.github.ljtfreitas.restify.http.client.hateoas.Links;
import com.github.ljtfreitas.restify.util.Tryable;

class HypermediaHalLinksSerializer extends ContainerSerializer<Links> implements ContextualSerializer {

	private static final long serialVersionUID = 1L;

	private final BeanProperty property;

	public HypermediaHalLinksSerializer() {
		this(null);
	}

	public HypermediaHalLinksSerializer(BeanProperty property) {
		super(TypeFactory.defaultInstance().constructType(Links.class));
		this.property = property;
	}

	@Override
	public void serialize(Links value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException, JsonGenerationException {

		Map<String, List<HalLink>> links = value.all().stream()
				.map(this::halLink)
					.collect(Collectors.groupingBy(h -> h.link.rel()));

		serializer(serializerProvider).serialize(links, jsonGenerator, serializerProvider);;
	}

	private MapSerializer serializer(SerializerProvider serializerProvider) throws JsonMappingException {
		TypeFactory typeFactory = serializerProvider.getConfig().getTypeFactory();

		JavaType keyType = typeFactory.constructType(String.class);

		JavaType valueType = typeFactory.constructCollectionType(ArrayList.class, Object.class);

		JavaType mapType = typeFactory.constructMapType(HashMap.class, keyType, valueType);

		MapSerializer mapSerializer = MapSerializer.construct(Collections.emptySet(), mapType, true, null, 
				serializerProvider.findKeySerializer(keyType, null), new HalLinkListSerializer(property), null);

		return mapSerializer;
	}

	private HalLink halLink(Link link) {
		return new HalLink(link, null);
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
			throws JsonMappingException {
		return new HypermediaHalLinksSerializer(property);
	}

	@Override
	public JavaType getContentType() {
		return null;
	}

	@Override
	public JsonSerializer<?> getContentSerializer() {
		return null;
	}

	public boolean isEmpty(List<Link> value) {
		return isEmpty(null, value);
	}

	public boolean isEmpty(SerializerProvider provider, List<Link> value) {
		return value.isEmpty();
	}

	@Override
	public boolean hasSingleElement(Links value) {
		return value.size() == 1;
	}

	@Override
	protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
		return null;
	}

	private class HalLinkListSerializer extends ContainerSerializer<Object> implements ContextualSerializer {

		private static final long serialVersionUID = 1L;

		private final BeanProperty property;

		public HalLinkListSerializer(BeanProperty property) {
			super(TypeFactory.defaultInstance().constructType(List.class));
			this.property = property;
		}

		@Override
		public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider provider)
				throws IOException, JsonGenerationException {

			Collection<HalLink> links = (Collection<HalLink>) value;

			if (!links.isEmpty()) {
				boolean single = (links.size() == 1);

				if (!single) {
					jsonGenerator.writeStartArray();
				}

				doSerialize(links, jsonGenerator, provider);

				if (!single) {
					jsonGenerator.writeEndArray();
				}

			}
		}

		private void doSerialize(Collection<HalLink> list, JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonGenerationException {

			list.forEach(link -> {
				Tryable.run(() -> provider.findValueSerializer(HalLink.class, property).serialize(link, jgen, provider));
			});
		}

		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public boolean hasSingleElement(Object value) {
			return false;
		}

		@Override
		public boolean isEmpty(Object value) {
			return false;
		}

		@Override
		public boolean isEmpty(SerializerProvider provider, Object value) {
			return false;
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property)
				throws JsonMappingException {
			return new HypermediaHalLinksSerializer(property);
		}
	}

	@JsonPropertyOrder(value = {"href", "templated", "type", "deprecation", "name", "profile", "title", "hreflang"})
	private class HalLink {

		private final Link link;
		private final String title;

		private HalLink(Link link, String title) {
			this.link = link;
			this.title = title;
		}

		@JsonProperty("href")
		private String href() {
			return link.href();
		}

		@JsonInclude(Include.NON_EMPTY)
		@JsonProperty("title")
		private String title() {
			return Optional.ofNullable(title).orElseGet(() -> link.title().orElse(null));
		}

		@JsonInclude(Include.NON_EMPTY)
		@JsonProperty("type")
		public String type() {
			return link.type().orElse(null);
		}

		@JsonInclude(Include.NON_EMPTY)
		@JsonProperty("name")
		public String name() {
			return link.property("name").orElse(null);
		}

		@JsonInclude(Include.NON_EMPTY)
		@JsonProperty("profile")
		public String profile() {
			return link.property("profile").orElse(null);
		}

		@JsonInclude(Include.NON_EMPTY)
		@JsonProperty("hreflang")
		public String hreflang() {
			return link.property("hreflang").orElse(null);
		}

		@JsonInclude(Include.NON_EMPTY)
		@JsonProperty("templated")
		@JsonSerialize(using = BooleanTrueSerializer.class)
		private boolean templated() {
			return link.templated();
		}

		@JsonInclude(Include.NON_EMPTY)
		@JsonProperty("deprecation")
		@JsonSerialize(using = BooleanTrueSerializer.class)
		private boolean deprecation() {
			return link.deprecation();
		}
	}
}
