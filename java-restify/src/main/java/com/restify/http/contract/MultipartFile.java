package com.restify.http.contract;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class MultipartFile {

	private final String name;
	private final String fileName;
	private final ContentType contentType;
	private final InputStream content;

	private MultipartFile(String name, ContentType contentType, String fileName, InputStream content) {
		this.name = name;
		this.fileName = fileName;
		this.contentType = contentType;
		this.content = content;
	}

	private MultipartFile(String name, ContentType contentType, InputStream content) {
		this(name, contentType, null, content);
	}

	private MultipartFile(String name, String fileName, InputStream content) {
		this(name, null, fileName, content);
	}

	private MultipartFile(String name, InputStream content) {
		this(name, null, null, content);
	}

	public String name() {
		return name;
	}

	public Optional<String> fileName() {
		return Optional.ofNullable(fileName);
	}

	public Optional<ContentType> contentType() {
		return Optional.ofNullable(contentType);
	}

	public InputStream content() {
		return content;
	}

	public static MultipartFile create(String name, String fileName, InputStream content) {
		return new MultipartFile(name, null, null, content);
	}

	public static MultipartFile create(String name, String fileName, ContentType contentType, InputStream content) {
		return new MultipartFile(name, contentType, fileName, content);
	}

	public static MultipartFile create(String name, File file) {
		try {
			ContentType contentType = Optional.ofNullable(Files.probeContentType(file.toPath())).map(ContentType::of)
					.orElse(null);
			return new MultipartFile(name, contentType, file.getName(), new FileInputStream(file));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static MultipartFile create(String name, ContentType contentType, File file) {
		try {
			return new MultipartFile(name, contentType, file.getName(), new FileInputStream(file));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static MultipartFile create(String name, Path path) {
		try {
			ContentType contentType = Optional.ofNullable(Files.probeContentType(path)).map(ContentType::of)
					.orElse(null);
			return new MultipartFile(name, contentType, path.getFileName().toString(), Files.newInputStream(path));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static MultipartFile create(String name, ContentType contentType, Path path) {
		try {
			return new MultipartFile(name, contentType, path.getFileName().toString(), Files.newInputStream(path));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static class ContentType {

		private final String type;

		public ContentType(String type) {
			this.type = type;
		}

		public String name() {
			return type;
		}

		public static ContentType of(String type) {
			return new ContentType(type);
		}
	}

}
