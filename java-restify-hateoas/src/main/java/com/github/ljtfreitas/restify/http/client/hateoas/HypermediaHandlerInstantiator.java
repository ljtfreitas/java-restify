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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.github.ljtfreitas.restify.http.client.hateoas.browser.HypermediaBrowser;
import com.github.ljtfreitas.restify.util.Tryable;

class HypermediaHandlerInstantiator extends HandlerInstantiator {

	private final Map<Class<?>, JsonDeserializer<?>> deserializers = new HashMap<>();

	public HypermediaHandlerInstantiator(HypermediaBrowser hypermediaBrowser) {
		this.deserializers.put(HypermediaLinksDeserializer.class, new HypermediaLinksDeserializer(hypermediaBrowser));
	}

	@Override
	public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
			Class<?> classType) {
		return findDeserializerOrCreate(classType);
	}

	@Override
	public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated,
			Class<?> classType) {
		return instantiate(classType);
	}

	@Override
	public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> classType) {
		return instantiate(classType);
	}

	@Override
	public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated,
			Class<?> classType) {
		return instantiate(classType);
	}

	@Override
	public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> classType) {
		return instantiate(classType);
	}

	private JsonDeserializer<?> findDeserializerOrCreate(Class<?> classType) {
		JsonDeserializer<?> jsonDeserializer = deserializers.get(classType);
		return Optional.ofNullable(jsonDeserializer).orElseGet(() -> instantiate(classType));
	}

	@SuppressWarnings("unchecked")
	private <T> T instantiate(Class<?> classType) {
		return (T) Tryable.of(() -> classType.newInstance());
	}
}
