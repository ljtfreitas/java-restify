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
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.github.ljtfreitas.restify.http.client.message.Encoding;
import com.github.ljtfreitas.restify.util.Try;

public class BufferedByteArrayHttpRequestBody implements BufferedHttpRequestBody {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 100;

	private final Charset charset;
	private final ByteArrayOutputStream source;
	private final BufferedOutputStream buffer;

	public BufferedByteArrayHttpRequestBody() {
		this(Encoding.UTF_8.charset());
	}

	public BufferedByteArrayHttpRequestBody(Charset charset) {
		this(charset, DEFAULT_BUFFER_SIZE);
	}

	public BufferedByteArrayHttpRequestBody(Charset charset, int size) {
		this.charset = charset;
		this.source = new ByteArrayOutputStream(size);
		this.buffer = new BufferedOutputStream(this.source);
	}

	@Override
	public OutputStream output() {
		return buffer;
	}

	@Override
	public byte[] asBytes() {
		return source.toByteArray();
	}

	@Override
	public String toString() {
		return Try.of(() -> source.toString(charset.name())).get();
	}

	public static BufferedHttpRequestBody empty() {
		return new BufferedByteArrayHttpRequestBody(Encoding.UTF_8.charset(), 0);
	}
}
