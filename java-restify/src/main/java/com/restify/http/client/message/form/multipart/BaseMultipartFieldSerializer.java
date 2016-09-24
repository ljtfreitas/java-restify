package com.restify.http.client.message.form.multipart;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.restify.http.client.request.HttpRequestMessage;
import com.restify.http.client.request.RestifyHttpMessageWriteException;

abstract class BaseMultipartFieldSerializer<T> implements MultipartFieldSerializer<T> {

	@Override
	public void write(String boundary, MultipartField<T> field, HttpRequestMessage httpRequestMessage) {
		try {
			OutputStream output = httpRequestMessage.output();

			output.write(boundary.getBytes());
			output.write('\r');
			output.write('\n');

			new MultipartFieldHeaders(field).writeOn(output);
			output.write('\r');
			output.write('\n');

			output.write(valueOf(field, httpRequestMessage.charset()));

			output.write('\r');
			output.write('\n');

			output.flush();

		} catch (IOException e) {
			throw new RestifyHttpMessageWriteException("Cannot write multipart/form-data field [" + field.name() + "]", e);
		}
	}

	class MultipartFieldHeaders {

		private final ContentDisposition contentDisposition;
		private final String contentType;

		private MultipartFieldHeaders(MultipartField<T> field) {
			this.contentDisposition = contentDispositionOf(field);
			this.contentType = contentTypeOf(field.value());
		}

		private void writeOn(OutputStream output) throws IOException {
			output.write(contentDisposition.toString().getBytes());
			output.write('\r');
			output.write('\n');

			if (contentType != null) {
				output.write("Content-Type: ".getBytes());
				output.write(contentType.getBytes());
				output.write('\r');
				output.write('\n');
			}
		}
	}

	protected String contentTypeOf(T value) {
		return null;
	}

	protected abstract ContentDisposition contentDispositionOf(MultipartField<T> field);

	protected abstract byte[] valueOf(MultipartField<T> field, Charset charset);

	class ContentDisposition {

		final String name;
		final String fileName;

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
