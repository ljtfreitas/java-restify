package com.restify.http.metadata;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.restify.http.contract.MultipartFile;
import com.restify.http.contract.MultipartFile.ContentType;

public class MultipartParameters {

	private Map<String, List<Object>> parameters = new LinkedHashMap<>();

	public void put(String name, String value) {
		doPut(name, value);
	}

	public void put(String name, File value) {
		doPut(name, MultipartFile.create(name, value));
	}

	public void put(String name, File value, ContentType contentType) {
		doPut(name, MultipartFile.create(name, contentType, value));
	}

	public void put(String name, Path value) {
		doPut(name, MultipartFile.create(name, value));
	}

	public void put(String name, Path value, ContentType contentType) {
		doPut(name, MultipartFile.create(name, contentType, value));
	}

	public void put(String name, String fileName, InputStream value) {
		doPut(name, MultipartFile.create(name, fileName, value));
	}

	public void put(String name, String fileName, ContentType contentType, InputStream value) {
		doPut(name, MultipartFile.create(name, fileName, contentType, value));
	}

	public void put(MultipartFile multipartFile) {
		doPut(multipartFile.name(), multipartFile);
	}

	public void doPut(String name, Object value) {
		parameters.compute(name, (k, v) -> Optional.ofNullable(v).orElseGet(() -> new ArrayList<>()))
			.add(value);
	}

	public Collection<Part<Object>> all() {
		return parameters.entrySet().stream().map(e -> new Part<>(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	public class Part<T> {

		private final String name;
		private final Collection<T> values;

		private Part(String name, Collection<T> values) {
			this.name = name;
			this.values = values;
		}

		public String name() {
			return name;
		}

		public Collection<T> values() {
			return values;
		}
	}
}
