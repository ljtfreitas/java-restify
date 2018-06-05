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
package com.github.ljtfreitas.restify.http.client.message.response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.github.ljtfreitas.restify.http.client.message.io.InputStreamContent;

public class BufferedHttpResponseBody implements HttpResponseBody {

	private final byte[] responseAsBytes;
	private final InputStream stream;

	private BufferedHttpResponseBody(byte[] responseAsBytes) {
		this.responseAsBytes = responseAsBytes;
		this.stream = new ByteArrayInputStream(responseAsBytes);
	}

	@Override
	public ByteBuffer asBuffer() {
		return ByteBuffer.wrap(responseAsBytes);
	}

	@Override
	public String asString() {
		return new String(responseAsBytes);
	}

	@Override
	public InputStream input() {
		return stream;
	}

	@Override
	public boolean empty() {
		return responseAsBytes.length == 0;
	}

	@Override
	public String toString() {
		return new String(responseAsBytes);
	}

	public static HttpResponseBody of(InputStream source) {
		InputStreamContent content = new InputStreamContent(source);
		return new BufferedHttpResponseBody(content.asBytes());
	}

	public static HttpResponseBody none() {
		return new BufferedHttpResponseBody(new byte[0]);
	}
}
