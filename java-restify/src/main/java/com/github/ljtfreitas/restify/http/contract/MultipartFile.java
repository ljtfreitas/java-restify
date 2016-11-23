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
package com.github.ljtfreitas.restify.http.contract;

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

}
