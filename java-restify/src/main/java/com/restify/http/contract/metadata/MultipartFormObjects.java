package com.restify.http.contract.metadata;

import static com.restify.http.util.Preconditions.isTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.restify.http.contract.Form.Field;
import com.restify.http.contract.MultipartForm;
import com.restify.http.contract.MultipartForm.MultipartField;

public class MultipartFormObjects {

	private static final MultipartFormObjects singleton = new MultipartFormObjects();

	private final Map<Class<?>, MultipartFormObject> cache = new ConcurrentHashMap<>();

	private MultipartFormObjects() {
	}

	public MultipartFormObject of(Class<?> formObjectType) {
		return get(formObjectType).orElseGet(() -> create(formObjectType));
	}

	private MultipartFormObject create(Class<?> formObjectType) {
		MultipartFormObject formObjectMetadata = new MultipartFormObject(formObjectType);

		Arrays.stream(formObjectType.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Field.class) || f.isAnnotationPresent(MultipartField.class))
			.forEach(f -> {
				String name = Optional.ofNullable(f.getAnnotation(MultipartField.class))
						.map(MultipartField::value).filter(n -> n != null && !"".equals(n))
							.orElseGet(() -> Optional.ofNullable(f.getAnnotation(Field.class))
									.map(Field::value).filter(n -> n != null && !"".equals(n))
										.orElse(f.getName()));

				formObjectMetadata.put(name, f);
		});

		cache.put(formObjectType, formObjectMetadata);

		return formObjectMetadata;
	}

	private Optional<MultipartFormObject> get(Class<?> formObjectType) {
		return Optional.ofNullable(cache.get(formObjectType));
	}

	public static MultipartFormObjects cache() {
		return singleton;
	}

	public static class MultipartFormObject {

		private final Class<?> type;
		private final Collection<FormObjectField> fields = new LinkedHashSet<>();

		private MultipartFormObject(Class<?> type) {
			isTrue(type.isAnnotationPresent(MultipartForm.class), "Your form class type must be annotated with @MultipartForm.");
			this.type = type;
		}

		public Collection<FormObjectField> fields() {
			return Collections.unmodifiableCollection(fields);
		}

		private void put(String name, java.lang.reflect.Field field) {
			isTrue(fields.add(new FormObjectField(name, field)), "Duplicate field [" + name + " on @MultipartForm object: " + type);
		}

		public class FormObjectField {

			private final String name;
			private final java.lang.reflect.Field field;

			FormObjectField(String name, java.lang.reflect.Field field) {
				this.name = name;
				this.field = field;
			}

			public String name() {
				return name;
			}

			public Object valueOn(Object source) {
				try {
					field.setAccessible(true);
					return field.get(source);

				} catch (Exception e) {
					throw new UnsupportedOperationException(e);
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
		}
	}

}
