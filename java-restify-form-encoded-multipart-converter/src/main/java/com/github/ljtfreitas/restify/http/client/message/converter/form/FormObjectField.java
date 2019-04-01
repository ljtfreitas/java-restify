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

import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.util.Objects;

import com.github.ljtfreitas.restify.http.contract.Form.Field;
import com.github.ljtfreitas.restify.http.contract.FormFieldSerializer;
import com.github.ljtfreitas.restify.http.contract.Notation;
import com.github.ljtfreitas.restify.util.Try;

public class FormObjectField {

	private final String name;
	private final boolean indexed;
	private final FormFieldSerializer serializer;
	private final java.lang.reflect.Field field;
	private final Notation notation;

	public FormObjectField(String name, boolean indexed, FormFieldSerializer serializer,
			java.lang.reflect.Field field, Notation notation) {
		this.name = name;
		this.indexed = indexed;
		this.serializer = serializer;
		this.field = field;
		this.notation = notation;
	}

	protected FormObjectField(FormObjectField delegate) {
		this(delegate.name, delegate.indexed, delegate.serializer, delegate.field, delegate.notation);
	}

	public String name() {
		return name;
	}

	public String delimiter() {
		return notation.delimiter();
	}

	public String nameWithNotation() {
		return notation.prefix() + name + notation.suffix();
	}

	public boolean indexed() {
		return indexed;
	}

	FormFieldSerializer serializer() {
		return serializer;
	}

	protected FormObjectField using(Notation notation) {
		return new FormObjectField(this.name, this.indexed, this.serializer, this.field, notation);
	}

	public Object get(Object source) throws Exception {
		field.setAccessible(true);
		return field.get(source);
	}

	public Object serialize(Object value) {
		return serializer.serialize(value.getClass(), value);
	}

	public void applyTo(Object object, Object value) {
		Try.run(() -> field.setAccessible(true))
		   .apply(signal -> field.set(object, valueOf(value)))
		   .error(UnsupportedOperationException::new)
		   .apply();
	}

	private Object valueOf(Object value) {
		Class<?> fieldType = field.getType();

		if (fieldType.isInstance(value)) {
			return value;

		} else if (fieldType == Byte.class || fieldType == byte.class) {
			return Byte.valueOf(value.toString());

		} else if (fieldType == Short.class || fieldType == short.class) {
			return Short.valueOf(value.toString());

		} else if (fieldType == Integer.class || fieldType == int.class) {
			return Integer.valueOf(value.toString());

		} else if (fieldType == Long.class || fieldType == long.class) {
			return Long.valueOf(value.toString());

		} else if (fieldType == Float.class || fieldType == float.class) {
			return Float.valueOf(value.toString());

		} else if (fieldType == Double.class || fieldType == double.class) {
			return Integer.valueOf(value.toString());

		} else if (fieldType == Boolean.class || fieldType == boolean.class) {
			return Integer.valueOf(value.toString());

		} else if (fieldType == Character.class || fieldType == char.class) {
			return Character.valueOf(value.toString().charAt(0));

		} else {
			return value;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, field);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FormObjectField) {
			FormObjectField that = (FormObjectField) obj;

			return Objects.equals(name, that.name)
				&& Objects.equals(field, that.field);

		} else {
			return false;
		}
	}

	public static FormObjectField of(java.lang.reflect.Field field, Notation notation) {
		isTrue(field.isAnnotationPresent(Field.class), "Your form field must be annotated with @Field.");

		Field annotation = field.getAnnotation(Field.class);

		String name = annotation.value().isEmpty() ? field.getName() : annotation.value();

		boolean indexed = annotation.indexed();

		FormFieldSerializer serializer = Try.of(annotation.serializer()::newInstance)
				.error(e -> new UnsupportedOperationException("Cannot create a instance of serializer " + annotation.serializer(), e))
					.get();

		return new FormObjectField(name, indexed, serializer, field, notation);
	}
}