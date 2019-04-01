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

import java.io.IOException;
import java.io.OutputStream;

public class TeeHttpRequestBody implements HttpRequestBody {

	private final OutputStream output;

	public TeeHttpRequestBody(HttpRequestBody source, OutputStream branch) {
		this.output = new BranchedOutputStream(source.output(), branch);
	}

	@Override
	public OutputStream output() {
		return output;
	}

	private class BranchedOutputStream extends OutputStream {

		private final OutputStream source;
		private final OutputStream branch;

		private BranchedOutputStream(OutputStream source, OutputStream branch) {
			this.source = source;
			this.branch = branch;
		}

		@Override
		public void write(int b) throws IOException {
			source.write(b);
			branch.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			source.write(b);
			branch.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			source.write(b, off, len);
			branch.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			source.flush();
			branch.flush();
		}

		@Override
		public void close() throws IOException {
			source.close();
			branch.close();
		}
	}

}
