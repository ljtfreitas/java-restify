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

import com.github.ljtfreitas.restify.util.Tryable;

public class RequestBody extends OutputStream {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 100;

	private final ByteArrayOutputStream source;
	private final BufferedOutputStream buffer;

	public RequestBody() {
		this(DEFAULT_BUFFER_SIZE);
	}

	public RequestBody(int size) {
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

	public boolean empty() {
		return source.size() != 0;
	}

	public void writeTo(OutputStream out) throws IOException {
		source.writeTo(out);
	}

	public byte[] toByteArray() {
		return source.toByteArray();
	}

	@Override
	public String toString() {
		return source.toString();
	}

	public String toString(String charsetName) {
		return Tryable.of(() -> source.toString(charsetName));
	}

	@Override
	public void close() throws IOException {
		buffer.close();
	}
}
