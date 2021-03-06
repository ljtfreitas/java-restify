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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.message.converter.HttpMessageWriteException;
import com.github.ljtfreitas.restify.http.client.message.request.HttpRequestMessage;

abstract class BaseMultipartFieldSerializer<T> implements MultipartFieldSerializer<T> {

	private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type: ";

	@Override
	public void write(String boundary, MultipartField<T> field, HttpRequestMessage httpRequestMessage) {
		try {
			OutputStream output = httpRequestMessage.body().output();

			output.write(boundary.getBytes());
			output.write('\r');
			output.write('\n');

			writeHeaders(field, output);

			output.write('\r');
			output.write('\n');

			writeContent(field, httpRequestMessage.charset(), output);

			output.write('\r');
			output.write('\n');

			output.flush();

		} catch (IOException e) {
			throw new HttpMessageWriteException("Cannot write multipart/form-data field [" + field.name() + "]", e);
		}
	}

	protected String contentTypeOf(T value) {
		return null;
	}

	private void writeHeaders(MultipartField<T> field, OutputStream output) throws IOException {
		ContentDisposition contentDisposition = contentDispositionOf(field);
		String contentType = field.contentType().orElseGet(() -> contentTypeOf(field.value()));

		output.write(contentDisposition.toString().getBytes());
		output.write('\r');
		output.write('\n');

		if (contentType != null) {
			output.write(CONTENT_TYPE_HEADER_NAME.getBytes());
			output.write(contentType.getBytes());
			output.write('\r');
			output.write('\n');
		}
	}

	protected abstract ContentDisposition contentDispositionOf(MultipartField<T> field);

	protected abstract void writeContent(MultipartField<T> field, Charset charset, OutputStream output) throws IOException;

	class ContentDisposition {

		final String name;
		final String fileName;

		ContentDisposition(String name) {
			this(name, null);
		}

		ContentDisposition(String name, String fileName) {
			this.name = name;
			this.fileName = fileName;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();

			builder.append("Content-Disposition: form-data; ")
				.append("name=\"")
					.append(name)
				.append("\"");

			if (fileName != null) {
				builder.append("; filename=\"")
							.append(fileName)
						.append("\"");
			}

			return builder.toString();
		}
	}
}
