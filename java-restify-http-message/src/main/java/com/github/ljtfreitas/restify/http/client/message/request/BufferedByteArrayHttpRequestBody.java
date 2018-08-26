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
package com.github.ljtfreitas.restify.http.client.message.request;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.util.Tryable;

public class BufferedByteArrayHttpRequestBody implements HttpRequestBody {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 100;

	private final Charset charset;
	private final BufferedOutput output;

	public BufferedByteArrayHttpRequestBody() {
		this(Encoding.UTF_8.charset());
	}

	public BufferedByteArrayHttpRequestBody(Charset charset) {
		this(charset, DEFAULT_BUFFER_SIZE);
	}

	public BufferedByteArrayHttpRequestBody(Charset charset, int size) {
		this.charset = charset;
		this.output = new BufferedOutput(size);
	}

	@Override
	public OutputStream output() {
		return output;
	}

	@Override
	public byte[] asBytes() {
		return output.source.toByteArray();
	}

	@Override
	public boolean empty() {
		return output.source.size() == 0;
	}

	@Override
	public String toString() {
		return Tryable.of(() -> output.source.toString(charset.name()));
	}

	@Override
	public void writeTo(OutputStream other) {
		Tryable.run(() -> output.source.writeTo(other));
	}

	private class BufferedOutput extends OutputStream {

		private final ByteArrayOutputStream source;
		private final BufferedOutputStream buffer;

		private BufferedOutput(int size) {
			this.source = new ByteArrayOutputStream(size);
			this.buffer = new BufferedOutputStream(source);
		}

		@Override
		public void write(byte[] b) throws IOException {
			buffer.write(b);
		}

		@Override
		public void write(int b) throws IOException {
			buffer.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			buffer.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			buffer.flush();
		}

		@Override
		public void close() throws IOException {
			buffer.close();
		}
	}
}
