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
package com.github.ljtfreitas.restify.http.client.message.converter.form.multipart;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

import com.github.ljtfreitas.restify.util.Try;

class FileMultipartFieldSerializer extends BaseMultipartFieldSerializer<File> {

	@Override
	public boolean supports(Class<?> type) {
		return File.class.isAssignableFrom(type);
	}

	@Override
	protected ContentDisposition contentDispositionOf(MultipartField<File> field) {
		return new ContentDisposition(field.name(), field.value().getName());
	}

	@Override
	protected String contentTypeOf(File value) {
		return Try.of(() -> Files.probeContentType(value.toPath()))
					.recover(IOException.class, e -> null)
						.get();
	}

	@Override
	protected void writeContent(MultipartField<File> field, Charset charset, OutputStream output) throws IOException {
		try {
			Files.copy(field.value().toPath(), output);
		} catch (IOException e) {
			throw new IOException("Cannot read data of parameter [" + field.name() + "] (File)", e);
		}
	}
}
