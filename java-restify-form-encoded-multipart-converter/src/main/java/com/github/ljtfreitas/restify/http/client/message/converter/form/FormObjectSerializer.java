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
package com.github.ljtfreitas.restify.http.client.message.converter.form;

import java.util.Optional;

import com.github.ljtfreitas.restify.http.contract.Parameters;
import com.github.ljtfreitas.restify.http.contract.Parameters.Parameter;
import com.github.ljtfreitas.restify.util.Try;

class FormObjectSerializer {

	private final FormObject form;
	private final boolean nested;
	private final String prefix;

	FormObjectSerializer(FormObject form) {
		this(form, !form.name().isEmpty(), null);
	}

	private FormObjectSerializer(FormObject form, boolean nested, String prefix) {
		this.form = form;
		this.nested = nested;
		this.prefix = prefix;
	}

	Parameters serialize(Object source) {
		return form.fields().values().stream()
				.reduce(new Parameters(prefix()),
					(a, b) -> Try.of(() -> serialize(b, source)).map(a::putAll).get(),
						(a, b) -> b);
	}

	private String prefix() {
		return Optional.ofNullable(prefix)
				.map(p -> p + form.prefix())
					.orElseGet(form::prefix);
	}

	private Parameters serialize(FormObjectField field, Object source) throws Exception {
		return new FormObjectFieldSerializer(field, nested).serialize(source);
	}

	class FormObjectFieldSerializer {

		private final FormObjectField field;
		private final boolean nested;
		private final String name;

		private FormObjectFieldSerializer(FormObjectField field, boolean nested) {
			this(field, nested, null);
		}

		private FormObjectFieldSerializer(FormObjectField field, boolean nested, String name) {
			this.field = field;
			this.nested = nested;
			this.name = name;
		}

		Parameters serialize(Object source) throws Exception {
			return doSerialize(field.get(source));
		}

		private Parameters doSerialize(Object value) throws Exception {
			if (value != null) {
				if (FormObject.is(value.getClass())) {
					return asNested(value);

				} else if (Iterable.class.isAssignableFrom(value.getClass())) {
					return asIterable(value);

				} else {
					return valueOf(value);
				}

			} else {
				return Parameters.empty();

			}
		}

		private Parameters asNested(Object value) {
			FormObject nested = FormObjects.cache().of(value.getClass()).using(form.notation());

			Parameters nestedParameters = new FormObjectSerializer(nested, true, nameAsPrefix()).serialize(value);

			return nestedParameters;
		}

		@SuppressWarnings("rawtypes")
		private Parameters asIterable(Object value) throws Exception {
			Iterable iterable = (Iterable) value;

			int position = 0;

			Parameters parameters = new Parameters();

			for (Object element : iterable) {
				String indexedName = field.indexed() ? name() + "[" + position + "]" : name();

				parameters = parameters.putAll(new FormObjectFieldSerializer(field, false, indexedName).doSerialize(element));

				position++;
			}

			return parameters;
		}

		private Parameters valueOf(Object value) {
			return Parameters.of(Parameter.of(name(), field.serialize(value).toString()));
		}

		private String name() {
			return Optional.ofNullable(name).orElseGet(() -> nested ? field.nameWithNotation() : field.name());
		}

		private String nameAsPrefix() {
			return name() + field.delimiter();
		}
	}
}