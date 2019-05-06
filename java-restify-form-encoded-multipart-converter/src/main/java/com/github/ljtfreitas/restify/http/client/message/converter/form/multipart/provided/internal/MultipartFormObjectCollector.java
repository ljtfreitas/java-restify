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
package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart.provided.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.client.message.converter.form.provided.internal.FormObject;
import com.github.ljtfreitas.restify.util.Try;

class MultipartFormObjectCollector {

	private final MultipartFormObject form;
	private final boolean nested;
	private final String prefix;

	public MultipartFormObjectCollector(MultipartFormObject form) {
		this(form, !form.name().isEmpty(), null);
	}

	private MultipartFormObjectCollector(MultipartFormObject form, boolean nested, String prefix) {
		this.form = form;
		this.nested = nested;
		this.prefix = prefix;
	}

	Collection<MultipartFormObjectFieldEntry> collect(Object source) {
		return form.fields().values().stream()
				.flatMap(field -> Try.of(() -> collect((MultipartFormObjectField) field, source)).get().stream())
					.collect(Collectors.toList());
	}

	private Collection<MultipartFormObjectFieldEntry> collect(MultipartFormObjectField field, Object source) throws Exception {
		return new MultipartFormObjectFieldCollector(field, nested).collect(source, prefix());
	}

	private String prefix() {
		return Optional.ofNullable(prefix)
				.map(p -> p + (nested ? form.prefixWithNotation() : form.prefix()))
					.orElseGet(form::prefix);
	}

	class MultipartFormObjectFieldCollector {

		private final MultipartFormObjectField field;
		private final boolean nested;
		private final String name;

		private MultipartFormObjectFieldCollector(MultipartFormObjectField field, boolean nested) {
			this(field, nested, null);
		}

		private MultipartFormObjectFieldCollector(MultipartFormObjectField field, boolean nested, String name) {
			this.field = field;
			this.nested = nested;
			this.name = name;
		}

		Collection<MultipartFormObjectFieldEntry> collect(Object source, String prefix) throws Exception {
			return doCollect(field.get(source), prefix);
		}

		private Collection<MultipartFormObjectFieldEntry> doCollect(Object value, String prefix) throws Exception {
			if (value != null) {
				if (FormObject.is(value.getClass())) {
					return collectAsNested(value, prefix);

				} else if (Iterable.class.isAssignableFrom(value.getClass())) {
					return collectAsIterable(value, prefix);

				} else {
					return entryOf(value, prefix);
				}

			} else {
				return Collections.emptyList();
			}
		}

		private Collection<MultipartFormObjectFieldEntry> collectAsNested(Object value, String prefix) {
			MultipartFormObject nested = MultipartFormObjects.cache().of(value.getClass()).using(form.notation());
			return new MultipartFormObjectCollector(nested, true, prefixed(prefix, nameAsPrefix())).collect(value);
		}

		@SuppressWarnings("rawtypes")
		private Collection<MultipartFormObjectFieldEntry> collectAsIterable(Object value, String prefix) throws Exception {
			Iterable iterable = (Iterable) value;

			int position = 0;

			Collection<MultipartFormObjectFieldEntry> entries = new LinkedList<>();

			for (Object element : iterable) {
				String indexedName = field.indexed() ? name() + "[" + position + "]" : name();

				entries.addAll(new MultipartFormObjectFieldCollector(field, false, indexedName).doCollect(element, prefix()));

				position++;
			}

			return entries;
		}

		private Collection<MultipartFormObjectFieldEntry> entryOf(Object value, String prefix) {
			return Collections.singleton(new MultipartFormObjectFieldEntry(field, name(prefix), field.serialize(value)));
		}

		private String name() {
			return Optional.ofNullable(name)
					.map(name -> nested ? nameAsNotation(name) : name)
						.orElseGet(() -> nested ? field.nameWithNotation() : field.name());
		}

		private String nameAsNotation(String name) {
			return form.notation().prefix() + name + form.notation().suffix();
		}

		private String name(String prefix) {
			return Optional.ofNullable(prefix)
					.map(p -> p + name())
						.orElseGet(this::name);
		}

		private String prefixed(String prefix, String name) {
			return Optional.ofNullable(prefix)
					.map(p -> p + name)
						.orElse(name);
		}

		private String nameAsPrefix() {
			return name() + field.delimiter();
		}
	}
}
