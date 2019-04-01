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

import static com.github.ljtfreitas.restify.util.Preconditions.isFalse;
import static com.github.ljtfreitas.restify.util.Preconditions.isTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.ljtfreitas.restify.http.contract.Form;
import com.github.ljtfreitas.restify.http.contract.Notation;
import com.github.ljtfreitas.restify.http.contract.Form.Field;

public class FormObject {

	private static final String EMPTY = "";
	private final Class<?> type;
	private final String name;
	private final Notation notation;
	private final Map<String, ? extends FormObjectField> fields;

	protected FormObject(Class<?> type, String name, Notation notation, Map<String, ? extends FormObjectField> fields) {
		this.type = type;
		this.name = name;
		this.notation = notation;
		this.fields = fields;
	}

	public Class<?> type() {
		return type;
	}

	public String name() {
		return name;
	}

	public String prefix() {
		return name.isEmpty() ? EMPTY : name + notation.delimiter();
	}

	public String prefixWithNotation() {
		String prefix = prefix();
		return prefix.isEmpty() ? EMPTY : notation.prefix() + prefix + notation.suffix();
	}

	public Map<String, ? extends FormObjectField> fields() {
		return fields;
	}

	public Notation notation() {
		return notation;
	}

	protected FormObject using(Notation notation) {
		Map<String, FormObjectField> newFields = fields.entrySet().stream()
			.map(e -> e.getValue().using(notation))
				.collect(Collectors.toMap(FormObjectField::name, Function.identity(), (a, b) -> b, LinkedHashMap::new));

		return new FormObject(this.type, this.name, notation, newFields);
	}

	public Optional<FormObjectField> fieldBy(String name) {
		return Optional.ofNullable(fields.get(name));
	}

	public String serialize(Object source) {
		isTrue(type.isInstance(source), "This FormObject can only serialize objects of class type [" + type + "]");

		return new FormObjectSerializer(this).serialize(source).queryString();
	}

	public static FormObject of(Class<?> formObjectType) {
		return of(formObjectType, true);
	}

	public static FormObject of(Class<?> formObjectType, boolean readFields) {
		isTrue(is(formObjectType), "Your form class type must be annotated with @Form.");

		Form annotation = formObjectType.getAnnotation(Form.class);

		Notation notation = annotation.notation();
		String name = annotation.value();

		Map<String, FormObjectField> fields = new LinkedHashMap<>();

		if (readFields) {
			Arrays.stream(formObjectType.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(Field.class))
					.forEach(field -> {
						FormObjectField formObjectField = FormObjectField.of(field, notation);

						isFalse(fields.containsKey(formObjectField.name()),
								"Duplicate field [" + name + " on @Form object: " + formObjectType);

						fields.put(formObjectField.name(), formObjectField);
			});
		}

		return new FormObject(formObjectType, name, notation, fields);
	}

	public static boolean is(Class<?> candidate) {
		return candidate.isAnnotationPresent(Form.class);
	}
}