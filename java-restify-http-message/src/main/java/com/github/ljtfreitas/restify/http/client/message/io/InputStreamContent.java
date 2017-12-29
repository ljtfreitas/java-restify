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
package com.github.ljtfreitas.restify.http.client.message.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public class InputStreamContent {

	public static final int DEFAULT_BUFFER_SIZE = 1024;

	private final InputStream source;
	private final int bufferSize;

	public InputStreamContent(InputStream source) {
		this(source, DEFAULT_BUFFER_SIZE);
	}

	public InputStreamContent(InputStream source, int bufferSize) {
		this.source = source;
		this.bufferSize = bufferSize;
	}

	public void transferTo(OutputStream output) {
		try {
			doTransfer(output);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public String asString() {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream(bufferSize);
			doTransfer(output);
			output.flush();
			output.close();
	
			return new String(output.toByteArray());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void doTransfer(OutputStream output) throws IOException {
		int len = 0;
		byte[] data = new byte[bufferSize];
		
		while ((len = source.read(data, 0, data.length)) != -1) {
			output.write(data, 0, len);
		}
	}
}
